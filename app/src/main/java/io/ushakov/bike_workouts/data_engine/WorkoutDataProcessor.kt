package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.db.entity.*
import io.ushakov.bike_workouts.db.repository.*
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.DateDifference
import io.ushakov.bike_workouts.util.getDifferenceBetweenDates
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
        //Write HR
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
        Log.d("DBG", "Location: ${location.speed}")

        // Ignore locations when standing in same place
        if (speed < 1 /*&& lastSpeed == 0f*/) {
            Log.d("DBG", "Speed is less then 1")
            return
        }

        Log.d("DBG", "speed: $speed meters/whatever")

        // Insert location
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
            //update Summary table
            updateSummary()
        }

    }

    suspend fun restoreWorkout(user: User, workout: Workout, summary: Summary, lastDuration: Duration?) {
        workoutUser = user
        activeWorkout = workout
        activeDuration = lastDuration
        totalWorkoutDuration = getWorkoutTotalDuration()

    }

    fun createWorkout(user: User, title: String, type: Int) {

        //TODO create a function resetAllParameters()
        activeWorkout = null
        activeDuration = null
        totalWorkoutDuration = 0L
        currentWorkoutDistance= 0.0
        workoutCalories = 0

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

            //Create Summary
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
            Log.d("DBG", "onGoingDuration.stopAt: ${onGoingDuration.stopAt}")

            coroutineScope.launch(Dispatchers.IO) {


                workoutRepository.setWorkoutStatus(workout.id, false)

                onGoingDuration.stopAt = Date()
                activeDuration!!.stopAt = Date()
                //TODO remove async after feature is tested
                val updateDurationJob = async { durationRepository.update(onGoingDuration) }
                if (updateDurationJob.await() > 0) {
                    //Log.d("DBG", "Duration updated at pause")
                } else {
                    //Log.d("DBG", "Failed to update Duration")
                }

                totalWorkoutDuration = getWorkoutTotalDuration()
                Log.d("DBG", "Total workout duration when paused: $totalWorkoutDuration")

                calculateCalories(totalWorkoutDuration)

                Log.d("DBG", "Workout paused")
            }
        }
        //totalWorkoutDuration = 0L

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

    fun stopWorkout() {

        val workout = activeWorkout ?: return
        val onGoingDuration = activeDuration ?: return

        CoroutineScope(Dispatchers.IO).launch {

            Log.d("DBG", "Total workout duration when stoped: $totalWorkoutDuration")

            if (totalWorkoutDuration > Constants.MINIMUM_WORKOUT_DURATION_MS) {

                workoutRepository.captureWorkoutFinishDate(workout.id, onGoingDuration.stopAt!!)

                calculateCalories(totalWorkoutDuration)
                updateSummary()

            } else {
                Log.d("DBG", "Deleting workout")
                CoroutineScope(Dispatchers.IO).launch {
                    workoutRepository.deleteById(workout.id)
                }
            }

        }

        activeWorkout = null
        currentWorkoutDistance = 0.0

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

    private fun calculateCalories(workoutDurationInMicroSeconds: Long) {
        val workout = activeWorkout ?: return
        val user = workoutUser ?: return

        workoutCalories =
            workoutCaloriesProcessor.getCalories(user, workout, workoutDurationInMicroSeconds)
        Log.d("DBG", "Calories: $workoutCalories")
    }

    private suspend fun getWorkoutTotalDuration() = withContext(Dispatchers.IO) {
        val workout = activeWorkout

        val workoutDurationJob = async {
            workout?.let { workoutRepository.getWorkoutDurations(it.id) }
        }

        val workoutDurationList = workoutDurationJob.await()
        Log.d("DBG", "workoutDurationList size ${workoutDurationList?.duration?.size}")

        var workoutDuration = 0L
        workoutDurationList?.duration?.forEach {
            Log.d("DBG", "durations end time ${it.stopAt}")

            workoutDuration += it.stopAt?.time?.minus(it.startAt.time)!!
        }
        Log.d("DBG", "Total work out duration from getWorkoutTotalDuration ${workoutDuration / 1000}")

        return@withContext workoutDuration
    }

    fun calculateTime(): String {

        var diff: DateDifference? = null

        if (activeDuration?.stopAt == null) {
            //Log.d("DBG", "activeDuration StopAt null. totalWorkoutDuration ${totalWorkoutDuration + (Date().time - activeDuration?.startAt!!.time)}")
            diff =
                getDifferenceBetweenDates(totalWorkoutDuration + (Date().time - activeDuration?.startAt!!.time))
        } else {
            //Log.d("DBG", "activeDuration Stop not null. totalWorkoutDuration $totalWorkoutDuration activeDuration?.startAt? ${activeDuration?.startAt}")
            diff = getDifferenceBetweenDates(totalWorkoutDuration)
        }

        return "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}"

    }
}