package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
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

    private var activeWorkout: Workout? = null

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

        // Ignore locations when standing in same place
        if(speed < 1 && lastSpeed == 0f) {
            return
        } else if(speed < 1 && lastSpeed != 0f) {
            lastSpeed = 0f
            speed = 0f
        }

        Log.d("DBG", "currentWorkoutDistance: $currentWorkoutDistance meters")

        // Insert location
        coroutineScope.launch(Dispatchers.IO) {

            if (currentWorkoutDistance == null) {
                val summary = summaryRepository.getSummaryForWorkout(workout.id)
                currentWorkoutDistance = summary?.distance ?: 0.0
            }


            currentWorkoutDistance =
                workoutDistanceProcessor.latestDistance(location, currentWorkoutDistance!!)

            locationRepository.locationDao.insert(
                io.ushakov.bike_workouts.db.entity.Location(
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

    fun restoreWorkout(user: User, workout: Workout, summary: Summary) {
        workoutUser = user
        activeWorkout = workout

    }

    fun createWorkout(user: User, title: String, type: Int) {
        Log.d("DBG", "Adding workout to DB, userId: ${user.id}, title: $title, type: $type")

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
                ))

            Log.d("DBG", "Workout created with ${workoutId} id")
        }
    }

    fun pauseWorkout() {
        val workout = activeWorkout ?: return

        calculateCalories(Date())

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, false)


            Log.d("DBG", "Workout paused")
        }
    }

    fun resumeWorkout() {
        val workout = activeWorkout ?: return

        coroutineScope.launch(Dispatchers.IO) {
            workoutRepository.setWorkoutStatus(workout.id, true)

            Log.d("DBG", "Workout resumed")
        }
    }

    fun stopWorkout() {
        val workout = activeWorkout ?: return

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

        activeWorkout = null
        currentWorkoutDistance = 0.0
    }


    private fun updateSummary() {
        val workout = activeWorkout ?: return

        coroutineScope.launch(Dispatchers.IO) {
            Log.d("WorkoutDataProcessor", "updating summary with distance $currentWorkoutDistance")
            val summary = summaryRepository.getSummaryForWorkout(workout.id) ?: return@launch

            summaryRepository.update(Summary(
                id = summary.id,
                workoutId = activeWorkout?.id ?: return@launch,
                distance = currentWorkoutDistance ?: 0.0,
                kiloCalories = workoutCalories
            ))
        }
    }

    private fun calculateCalories(endTime: Date) {
        val workout = activeWorkout ?: return
        val user = workoutUser ?: return

        workoutCalories =
            workoutCaloriesProcessor.getCalories(user, workout, endTime)
    }
}