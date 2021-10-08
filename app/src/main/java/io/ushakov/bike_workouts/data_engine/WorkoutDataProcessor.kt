package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.repository.HeartRateRepository
import io.ushakov.bike_workouts.db.repository.LocationRepository
import io.ushakov.bike_workouts.db.repository.SummaryRepository
import io.ushakov.bike_workouts.db.repository.WorkoutRepository
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
) {
    private val workoutDistanceProcessor = WorkoutDistanceProcessor()

    val activeWorkout by lazy { workoutRepository.unfinishedWorkout }

    var currentWorkoutDistance: Double = 0.0

    companion object {
        private var instance: WorkoutDataProcessor? = null

        fun initialize(
            workoutRepository: WorkoutRepository,
            heartRateRepository: HeartRateRepository,
            locationRepository: LocationRepository,
            summaryRepository: SummaryRepository,
        ) {
            instance = WorkoutDataProcessor(
                workoutRepository,
                heartRateRepository,
                locationRepository,
                summaryRepository
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

    fun processData(heartRateValue: Int, location: Location) {

        val workout = activeWorkout.value ?: return

        val timestamp = Date()

        //TODO calculate distance
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

    fun createWorkout(userId: Long, title: String, type: String) {
        Log.d("DBG", "Adding workout to DB, userId: ${userId}, title: $title, type: $type")

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

            Log.d("DBG", "Workout created with ${workoutId.await()} id")
        }
    }

    fun stopWorkout() {
//        Log.d("DBG", "Total distance covered: $currentWorkoutDistance meters")

        val workout = activeWorkout.value ?: return

        CoroutineScope(Dispatchers.IO).launch {
            //val workoutUpdateId = async {
            workoutRepository.captureWorkoutFinishDate(workout.id)
            //}
            // If workoutUpdateId.await() value is > 0 then update is successful
            // This value represent number of entries updated in DB
            // Can be useful if want to check if workout is updated
            // Same happens with delete operation
            // else app will crush
        }
    }

    fun deleteCurrentWorkout() {

        val workout = activeWorkout.value ?: return
        //Saving currentWorkoutId, it may set to default by reset() before coroutine scope start,
        //that's why not passing whole workout to delete.
        CoroutineScope(Dispatchers.IO).launch {
            workoutRepository.deleteById(workout.id)
        }

        //Following way of deleting can have some issue e.g.
        // Wee need to reset current values to default and it is
        // posssible that default values goes with Workout entity for deletion
        /*CoroutineScope(Dispatchers.IO).launch {
            val workoutId = async {
                workoutRepository.delete(
                    Workout(
                        currentWorkoutUserId,
                        currentWorkoutUserId,
                        currentWorkoutTitle,
                        currentWorkoutType,
                        currentWorkoutStartTime,
                        Date()
                    )
                )
            }
        }*/
    }
}