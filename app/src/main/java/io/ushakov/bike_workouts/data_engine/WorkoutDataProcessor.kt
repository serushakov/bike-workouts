package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.db.repository.HeartRateRepository
import io.ushakov.bike_workouts.db.repository.LocationRepository
import io.ushakov.bike_workouts.db.repository.SummaryRepository
import io.ushakov.bike_workouts.db.repository.WorkoutRepository
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
    private val coroutineScope: CoroutineScope
) {
    private val workoutDistanceProcessor = WorkoutDistanceProcessor()

    val activeWorkout by lazy { workoutRepository.unfinishedWorkout }

    var currentWorkoutDistance: Double = 0.0
    var currentWorkoutDuration: Long = 0


    companion object {
        private var instance: WorkoutDataProcessor? = null

        fun initialize(
            workoutRepository: WorkoutRepository,
            heartRateRepository: HeartRateRepository,
            locationRepository: LocationRepository,
            summaryRepository: SummaryRepository,
            coroutineScope: CoroutineScope
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository,
                coroutineScope
            )
        }

        fun getInstance(): WorkoutDataProcessor {
            return instance!!
        }


        /*fun startWorkout() {
            //TODO create a boolean in WorkoutDataReceiver whose value can be set from here.
            // This boolean will enable data writing to DB
            //println("Workout started")
        }*/

        //TODO do not let MainActivity.startWorkout() call startWorkoutService()
        // until currentWorkoutId is not generated


    }

    fun processData(heartRateValue: Int?, location: Location?) {
        val workout = activeWorkout.value ?: return

        val timestamp = Date()

        //TODO calculate distance

        if (location != null) {
            currentWorkoutDistance += workoutDistanceProcessor.latestDistance(location)
            Log.d("DBG", "currentWorkoutDistance: $currentWorkoutDistance meters")


            //TODO calculate kCal

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
        }

        if (heartRateValue != null) {

            //Write HR
            CoroutineScope(Dispatchers.IO).launch {
                val heartRateId = async {
                    heartRateRepository.insert(
                        HeartRate(
                            workout.id,
                            heartRateValue,
                            timestamp
                        )
                    )
                }
                Log.d("DBG", "heartRateId: ${heartRateId.await()}")
            }
        }

    }

    fun createWorkout(userId: Long, title: String, type: String) {
        Log.d("DBG", "Adding workout to DB, userId: ${userId}, title: $title, type: $type")

        coroutineScope.launch(Dispatchers.IO) {
            val workoutId = async {
                workoutRepository.insert(
                    Workout(
                        userId = userId,
                        title = title,
                        type = type,
                        startAt = Date()
                    )
                )
            }
            Log.d("DBG", "Workout created with ${workoutId.await()} id")
        }

    }

    fun pauseWorkout() {
        val workout = activeWorkout.value ?: return


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
//        Log.d("DBG", "Total distance covered: $currentWorkoutDistance meters")

        val workout = activeWorkout.value?.clone() ?: return

        val timeDifference =
            Date().time - workout.startAt.time

        Log.d("DBG", "timeDifference $timeDifference")

        if (timeDifference > Constants.MINIMUM_WORKOUT_DURATION_MS) {
            Log.d("DBG", "Stopping workout")

            CoroutineScope(Dispatchers.IO).launch {
                //val workoutUpdateId = async {
                workoutRepository.captureWorkoutFinishDate(workout.id)

                // TODO: Create workout summary

                // If workoutUpdateId.await() value is > 0 then update is successful
                // This value represent number of entries updated in DB
                // Can be useful if want to check if workout is updated
                // Same happens with delete operation
                // else app will crush
            }
        } else {
            Log.d("DBG", "Deleting workout")
            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.deleteById(workout.id)
            }
        }


    }
}