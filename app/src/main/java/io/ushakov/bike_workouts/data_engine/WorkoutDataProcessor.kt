package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.repository.*
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
    private val userRepository: UserRepository
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
            userRepository: UserRepository
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository,
                userRepository
            )
        }

        fun getInstance(): WorkoutDataProcessor {
            return instance!!
        }
        //TODO do not let MainActivity.startWorkout() call startWorkoutService()
        // until currentWorkoutId is not generated
    }

    fun processHeartRate(heartRateValue: Int) {
        val workout = activeWorkout.value ?: return
        val timestamp = Date()
        //Write HR
        CoroutineScope(Dispatchers.IO).launch {
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
        CoroutineScope(Dispatchers.IO).launch {
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
        updateSummary(workout.id)
    }

    fun createWorkout(userId: Long, title: String, type: Int) {
        //Create workout
        CoroutineScope(Dispatchers.IO).launch {
            val workoutId = async {
                workoutRepository.insert(
                    Workout(
                        userId,
                        title,
                        type,
                        Date(),
                        null
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
        //Fetch user so that we can have Age and weight to calculate kCal
        CoroutineScope(Dispatchers.IO).launch {
            val user = async {
                userRepository.getUserById(userId)
            }
            workoutUser = user.await()
        }


    }

    fun stopWorkout() {
        val workout = activeWorkout.value ?: return
        CoroutineScope(Dispatchers.IO).launch {
            workoutRepository.captureWorkoutFinishDate(workout.id)
        }
        workoutCalories = workoutCaloriesProcessor.getCalories(workoutUser!!, workout)
        updateSummary(workout.id)
        workoutDistanceProcessor.resetDistance()
    }

    private fun updateSummary(workoutId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            summaryRepository.update(Summary(
                workoutId,
                currentWorkoutDistance,
                workoutCalories
            ))
        }
    }

    fun deleteCurrentWorkout() {
        val workout = activeWorkout.value ?: return
        CoroutineScope(Dispatchers.IO).launch {
            workoutRepository.deleteById(workout.id)
        }
    }
}