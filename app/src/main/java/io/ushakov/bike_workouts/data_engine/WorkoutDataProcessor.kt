package io.ushakov.bike_workouts.data_engine

import android.location.Location
import io.ushakov.bike_workouts.db.entity.*
import io.ushakov.bike_workouts.db.repository.*
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.DateDifference
import io.ushakov.bike_workouts.util.calculateWorkoutDuration
import kotlinx.coroutines.*
import java.util.*

class WorkoutDataProcessor(
    private val workoutRepository: WorkoutRepository,
    private val heartRateRepository: HeartRateRepository,
    private val locationRepository: LocationRepository,
    private val summaryRepository: SummaryRepository,
    private val durationRepository: DurationRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val workoutDistanceProcessor = WorkoutDistanceProcessor()
    private val workoutCaloriesProcessor = WorkoutCaloriesProcessor()
    private var workoutUser: User? = null

    private var activeWorkout: Workout? = null

    // TODO save this to state
    private var activeDuration: Duration? = null
    private var totalWorkoutDuration: Long = 0L

    private var currentWorkoutDistance: Double? = null
    private var workoutCalories: Int = 0
    private var lastSpeed: Float? = null

    companion object {
        private var instance: WorkoutDataProcessor? = null

        fun initialize(
            workoutRepository: WorkoutRepository,
            heartRateRepository: HeartRateRepository,
            locationRepository: LocationRepository,
            summaryRepository: SummaryRepository,
            durationRepository: DurationRepository,
            coroutineScope: CoroutineScope,
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository,
                durationRepository,
                coroutineScope,
            )
        }

        fun getInstance(): WorkoutDataProcessor {
            return instance!!
        }
    }

    fun processHeartRate(heartRateValue: Int) {
        val workout = activeWorkout ?: return
        val timestamp = Date()

        coroutineScope.launch(Dispatchers.IO) {
            heartRateRepository.insert(
                HeartRate(
                    workout.id,
                    heartRateValue,
                    timestamp
                )
            )
        }
    }

    fun processLocation(location: Location) {
        val workout = activeWorkout ?: return
        val timestamp = Date()

        var speed = location.speed

        // Ignore locations when standing in same place
        if (speed < 1 && lastSpeed == 0f) {
            return
        } else if (speed < 1) {
            speed = 0f
            lastSpeed = 0f
        } else {
            lastSpeed = speed
        }

        coroutineScope.launch(Dispatchers.IO) {
            if (currentWorkoutDistance == null) {
                val summary = summaryRepository.getSummaryForWorkout(workout.id)
                currentWorkoutDistance = summary?.distance ?: 0.0
            }

            currentWorkoutDistance =
                workoutDistanceProcessor.latestDistance(location, currentWorkoutDistance!!)

            locationRepository.locationDao.insert(
                Location(
                    workout.id,
                    location.latitude,
                    location.longitude,
                    speed,
                    location.altitude.toFloat(),
                    timestamp
                )
            )

            updateSummary()
        }

    }

    suspend fun restoreWorkout(
        user: User,
        workout: Workout,
        summary: Summary,
        lastDuration: Duration?,
    ) {
        workoutUser = user
        activeWorkout = workout
        activeDuration = lastDuration
        totalWorkoutDuration = getWorkoutTotalDuration()
        currentWorkoutDistance = summary.distance
    }

    fun createWorkout(user: User, title: String, type: Int) {
        resetAllParameters()
        workoutUser = user

        coroutineScope.launch(Dispatchers.IO) {
            val workout = Workout(
                userId = user.id,
                title = title,
                type = type,
                startAt = Date()
            )

            val workoutId = workoutRepository.insert(workout)
            workout.id = workoutId
            activeWorkout = workout

            summaryRepository.insert(
                Summary(
                    workoutId = workoutId,
                    distance = currentWorkoutDistance ?: 0.0,
                    kiloCalories = workoutCalories
                )
            )

            activeDuration = Duration(0, workoutId, workout.startAt, null)
            val durationId = durationRepository.insert(activeDuration!!)
            activeDuration!!.id = durationId
        }
    }

    fun pauseWorkout() {
        val workout = activeWorkout ?: return
        val onGoingDuration = activeDuration ?: return

        if (onGoingDuration.stopAt == null) {
            coroutineScope.launch(Dispatchers.IO) {
                workoutRepository.setWorkoutStatus(workout.id, false)

                onGoingDuration.stopAt = Date()
                activeDuration!!.stopAt = Date()
                durationRepository.update(onGoingDuration)

                val averageSpeed = locationRepository.getWorkoutAverageSpeed(workout.id)

                totalWorkoutDuration = getWorkoutTotalDuration()
                calculateCalories(currentWorkoutDistance ?: 0.0, averageSpeed ?: 0.0)
            }
        }
    }

    fun resumeWorkout() {

        val workout = activeWorkout ?: return

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, true)

            val newDuration = Duration(0, workout.id, Date(), null)
            val durationId = durationRepository.insert(newDuration)
            newDuration.id = durationId
            activeDuration = newDuration
        }
    }

    suspend fun stopWorkout(): Long? {
        val workout = activeWorkout ?: return null
        val onGoingDuration = activeDuration ?: return null

        return withContext(Dispatchers.IO) {
            val averageSpeed = locationRepository.getWorkoutAverageSpeed(workout.id) ?: 0.0
            val workoutDistance = currentWorkoutDistance ?: 0.0
            var shouldReturnWorkoutId = true

            val actualWorkoutDuration =
                if (averageSpeed == 0.0) 0.0 else workoutDistance / averageSpeed

            // Workout should be of minimum length in order to be saved
            if (actualWorkoutDuration > Constants.MINIMUM_WORKOUT_DURATION_S) {
                workoutRepository.captureWorkoutFinishDate(workout.id, onGoingDuration.stopAt!!)

                locationRepository.getWorkoutAverageSpeed(workout.id)?.let {
                    calculateCalories(currentWorkoutDistance ?: 0.0, it)
                }
                updateSummary()

            } else {
                workoutRepository.deleteById(workout.id)
                shouldReturnWorkoutId = false
            }

            resetAllParameters()

            return@withContext if (shouldReturnWorkoutId) workout.id else null
        }

    }


    private fun updateSummary() {
        val workout = activeWorkout ?: return

        coroutineScope.launch(Dispatchers.IO) {
            val summary = summaryRepository.getSummaryForWorkout(workout.id) ?: return@launch

            summaryRepository.update(
                Summary(
                    id = summary.id,
                    workoutId = activeWorkout?.id ?: return@launch,
                    distance = currentWorkoutDistance ?: 0.0,
                    kiloCalories = workoutCalories
                )
            )
        }
    }

    private fun calculateCalories(distance: Double, averageSpeed: Double) {
        val workout = activeWorkout ?: return
        val user = workoutUser ?: return

        /**
         * This way we calculate actual cycling time. If we take
         * workout duration directly, the calories will be inaccurate.
         *
         * distance is calculated in meters, averageSpeed is m/s, so cyclingDuration is seconds
         */
        val cyclingDuration = distance / averageSpeed

        workoutCalories =
            workoutCaloriesProcessor.getCalories(user.weight, workout.type, cyclingDuration)
    }

    private suspend fun getWorkoutTotalDuration() = withContext(Dispatchers.IO) {
        val workout = activeWorkout

        val workoutDurations =
            workout?.let { workoutRepository.getWorkoutDurations(it.id) }?.duration
                ?: return@withContext 0

        return@withContext calculateWorkoutDuration(workoutDurations)
    }

    fun calculateTime(): String {
        val diff = if (activeDuration?.stopAt == null) {
            DateDifference.fromDuration(totalWorkoutDuration + (Date().time - activeDuration?.startAt!!.time))
        } else {
            DateDifference.fromDuration(totalWorkoutDuration)
        }

        return "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}"

    }

    private fun resetAllParameters() {
        activeWorkout = null
        activeDuration = null
        totalWorkoutDuration = 0L
        currentWorkoutDistance = null
        workoutCalories = 0
        lastSpeed = null
    }
}