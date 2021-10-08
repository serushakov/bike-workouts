package io.ushakov.bike_workouts.data_engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.repository.HeartRateRepository
import io.ushakov.bike_workouts.db.repository.LocationRepository
import io.ushakov.bike_workouts.db.repository.SummaryRepository
import io.ushakov.bike_workouts.db.repository.WorkoutRepository
import io.ushakov.bike_workouts.util.Constants.DEFAULT_USER_ID
import io.ushakov.bike_workouts.util.Constants.DEFAULT_WORKOUT_ID
import io.ushakov.bike_workouts.util.Constants.DEFAULT_WORKOUT_TITLE
import io.ushakov.bike_workouts.util.Constants.DEFAULT_WORKOUT_TYPE
import io.ushakov.bike_workouts.util.Constants.INITIAL_DISTANCE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class WorkoutDataProcessor() {

    constructor(appContext: Context) : this() {
        Log.d("DBG", "Constructor call")
        context = appContext
        context?.let {
            workoutRepository = (it as WorkoutApplication).workoutRepository
            heartRateRepository = (it as WorkoutApplication).heartRateRepository
            locationRepository = (it as WorkoutApplication).locationRepository
            summaryRepository = (it as WorkoutApplication).summaryRepository
        }
    }

    private val dummyVariable = null

    private val workoutDistanceProcessor = WorkoutDistanceProcessor()
    //private var distance: Double = INITIAL_DISTANCE

    // object scope

    //private val recentLocation = location
    //private val recentHeartRate = heartRateValue


    //val currentWorkoutId = WorkoutDataProcessor.currentWorkoutId

    fun processData(heartRateValue: Int, location: Location) {
        //Log.d("DBG", "WorkoutDataProcessor--Heart BPM: $heartRateValue, Location: $location")


        val timestamp = Date()
        //TODO write location to DB
        if (currentWorkoutId != DEFAULT_WORKOUT_ID &&
            currentWorkoutTitle != DEFAULT_WORKOUT_TITLE &&
            currentWorkoutType != DEFAULT_WORKOUT_TYPE
        ) {

            //TODO calculate distance
            currentWorkoutDistance += workoutDistanceProcessor.latestDistance(location)
            Log.d("DBG", "currentWorkoutDistance: $currentWorkoutDistance meters")


            //TODO calculate kCal
            CoroutineScope(Dispatchers.IO).launch {
                val locationId = async {
                    locationRepository.locationDoa.insert(
                        io.ushakov.bike_workouts.db.entity.Location(
                            currentWorkoutId,
                            location.latitude,
                            location.longitude,
                            location.speed,
                            location.speed,
                            timestamp
                        )
                    )
                }
                //Log.d("DBG", "locationId: ${locationId.await()}")
            }

            //Write HR
            CoroutineScope(Dispatchers.IO).launch {
                val heartRateId = async {
                    heartRateRepository.insert(
                        HeartRate(
                            currentWorkoutId,
                            heartRateValue,
                            timestamp
                        )
                    )
                }
                //Log.d("DBG", "heartRateId: ${heartRateId.await()}")
            }

        } else {
            Log.d(
                "DBG",
                "Unable to process data, workout id, title or type is still not set and using default value"
            )
        }
    }

    //Class scope
    companion object {
        //var temp: String
        //TODO find another wat to pass Context.
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        /*init {

            temp = "WorkoutDataProcessor Initialized"
            Log.d("DBG", "$temp")
        }*/

        //TODO make sure context is not null.

        //private var testRepository: WorkoutRepository
        private lateinit var workoutRepository: WorkoutRepository
        private lateinit var heartRateRepository: HeartRateRepository
        private lateinit var locationRepository: LocationRepository
        private lateinit var summaryRepository: SummaryRepository

        /*val workoutApplication = context?.let {
            workoutRepository = (it as WorkoutApplication).workoutRepository
            heartRateRepository = it.heartRateRepository
            locationRepository = it.locationRepository
            summaryRepository = it.summaryRepository
        }*/

        //val distanceProcessor: WorkoutDistanceProcessor = WorkoutDistanceProcessor()


        // TODO only getter, no setter
        var currentWorkoutDistance: Double = INITIAL_DISTANCE
        var currentWorkoutId: Long = DEFAULT_WORKOUT_ID
        private var currentWorkoutTitle: String = DEFAULT_WORKOUT_TITLE
        private var currentWorkoutType: String = DEFAULT_WORKOUT_TYPE
        var currentWorkoutUserId: Long = DEFAULT_USER_ID
        var isWorkoutOngoing: Boolean = false
        var currentWorkoutStartTime = Date()


        /*fun startWorkout() {
            //TODO create a boolean in WorkoutDataReceiver whose value can be set from here.
            // This boolean will enable data writing to DB
            //println("Workout started")
        }*/

        //TODO do not let MainActivity.startWorkout() call startWorkoutService()
        // until currentWorkoutId is not generated
        fun createWorkout(userId: Long, title: String, type: String) {
            //TODO check if all these are not null, only then write to DB
            currentWorkoutStartTime = Date()
            if (context == null) {
                Log.d("DBG", "Context is still null")
            }
            Log.d("DBG", "Adding workout to DB, userId: ${userId}, title: $title, type: $type")

            CoroutineScope(Dispatchers.IO).launch {
                val workoutId = async {
                    workoutRepository.insert(
                        Workout(
                            userId,
                            title,
                            type,
                            currentWorkoutStartTime,
                            null
                        )
                    )
                }
                currentWorkoutId = workoutId.await()
                Log.d("DBG", "Workout created with $currentWorkoutId id")
                if (currentWorkoutId != DEFAULT_WORKOUT_ID) {
                    isWorkoutOngoing = true
                    currentWorkoutTitle = title
                    currentWorkoutType = type
                }

            }
        }

        fun stopWorkout() {
            Log.d("DBG", "Total distance covered: $currentWorkoutDistance meters")

            val workoutIdToUpdate = currentWorkoutId
            isWorkoutOngoing = false
            CoroutineScope(Dispatchers.IO).launch {
                //val workoutUpdateId = async {
                workoutRepository.update(workoutIdToUpdate)
                //}
                // If workoutUpdateId.await() value is > 0 then update is successful
                // This value represent number of entries updated in DB
                // Can be useful if want to check if workout is updated
                // Same happens with delete operation
                // else app will crush
            }
            reset()
        }

        fun deleteCurrentWorkout() {
            //Saving currentWorkoutId, it may set to default by reset() before coroutine scope start,
            //that's why not passing whole workout to delete.
            val workoutIdToDelete = currentWorkoutId
            CoroutineScope(Dispatchers.IO).launch {
                workoutRepository.deleteById(workoutIdToDelete)
            }
            reset()
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

        private fun reset() {
            //deleteCurrentWorkout()
            currentWorkoutDistance = INITIAL_DISTANCE
            currentWorkoutId = DEFAULT_WORKOUT_ID
            currentWorkoutTitle = DEFAULT_WORKOUT_TITLE
            currentWorkoutType = DEFAULT_WORKOUT_TYPE
            currentWorkoutUserId = DEFAULT_USER_ID
            isWorkoutOngoing = false
        }
    }
}