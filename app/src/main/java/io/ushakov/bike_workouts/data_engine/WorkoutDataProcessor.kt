package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.repository.*
import io.ushakov.bike_workouts.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class WorkoutDataProcessor(
    private val workoutRepository: WorkoutRepository,
    private val heartRateRepository: HeartRateRepository,
    private val locationRepository: LocationRepository,
    private val summaryRepository: SummaryRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val workoutDistanceProcessor = WorkoutDistanceProcessor()
    private val workoutCaloriesProcessor = WorkoutCaloriesProcessor()
    private var workoutUser: User? = null

    val activeWorkout by lazy { workoutRepository.unfinishedWorkout }

    private var currentWorkoutDistance: Double = 0.0
    private var workoutCalories: Int = 0

    companion object {
        private var instance: WorkoutDataProcessor? = null

        fun initialize(
            workoutRepository: WorkoutRepository,
            heartRateRepository: HeartRateRepository,
            locationRepository: LocationRepository,
            summaryRepository: SummaryRepository,
            coroutineScope: CoroutineScope,
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository,
                coroutineScope,
            )
        }

        fun getInstance(): WorkoutDataProcessor {
            return instance!!
        }
    }

    fun processHeartRate(heartRateValue: Int) {
        val workout = activeWorkout.value ?: return
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
        val workout = activeWorkout.value ?: return
        val timestamp = Date()

        currentWorkoutDistance = workoutDistanceProcessor.latestDistance(location)
        Log.d("DBG", "currentWorkoutDistance: $currentWorkoutDistance meters")

        // Insert location
        coroutineScope.launch(Dispatchers.IO) {
            val locationId = async {
                locationRepository.locationDoa.insert(
                    io.ushakov.bike_workouts.db.entity.Location(
                        workout.id,
                        location.latitude,
                        location.longitude,
                        location.speed,
                        location.speed,
                        timestamp
                    )
                )
            }
            Log.d("DBG", "locationId: ${locationId.await()}")
        }
        //update Summary table
        updateSummary()
    }

    fun createWorkout(user: User, title: String, type: Int) {
        Log.d("DBG", "Adding workout to DB, userId: ${user.id}, title: $title, type: $type")

        workoutUser = user

        coroutineScope.launch(Dispatchers.IO) {
            val workoutId = async {
                workoutRepository.insert(
                    Workout(
                        userId = user.id,
                        title = title,
                        type = type,
                        startAt = Date()
                    )
                )
            }

            //Create Summary
            summaryRepository.insert(
                Summary(
                    workoutId.await(),
                    currentWorkoutDistance,
                    workoutCalories
                ))

            Log.d("DBG", "Workout created with ${workoutId.await()} id")
        }
    }

    fun pauseWorkout() {
        val workout = activeWorkout.value ?: return

        calculateCalories(Date())

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, false)


            Log.d("DBG", "Workout paused")
        }
    }

    fun resumeWorkout() {
        val workout = activeWorkout.value ?: return

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, true)

            Log.d("DBG", "Workout resumed")
        }
    }

    fun stopWorkout() {
        val workout = activeWorkout.value ?: return

        val finishTime = Date()

        val timeDifference = finishTime.time - workout.startAt.time

        Log.d("DBG", "timeDifference $timeDifference")

        if (timeDifference > Constants.MINIMUM_WORKOUT_DURATION_MS) {
            calculateCalories(finishTime)
            updateSummary()

            Log.d("DBG", "Stopping workout")

            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.captureWorkoutFinishDate(workout.id, finishAt = finishTime)
            }
        } else {
            Log.d("DBG", "Deleting workout")
            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.deleteById(workout.id)
            }
        }

        workoutDistanceProcessor.resetDistance()

    }


    private fun updateSummary() {
        CoroutineScope(Dispatchers.IO).launch {
            summaryRepository.update(Summary(
                workoutId = activeWorkout.value?.id ?: return@launch,
                currentWorkoutDistance,
                workoutCalories
            ))
        }
    }

    private fun calculateCalories(endTime: Date) {
        val workout = activeWorkout.value ?: return

        workoutCalories =
            workoutCaloriesProcessor.getCalories(workoutUser!!, workout, endTime)
    }
}