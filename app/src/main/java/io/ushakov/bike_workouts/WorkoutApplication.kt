package io.ushakov.bike_workouts

import android.app.Application
import android.app.Notification
import io.ushakov.bike_workouts.db.WorkoutDatabase
import io.ushakov.bike_workouts.db.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import android.app.NotificationManager

import android.app.NotificationChannel
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor


import io.ushakov.bike_workouts.util.Constants.CHANNEL_ID
import io.ushakov.bike_workouts.util.Constants.CHANNEL_NAME
import kotlinx.coroutines.runBlocking


class WorkoutApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy {
        WorkoutDatabase.getDatabase(this, applicationScope)
    }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao()) }
    val locationRepository by lazy { LocationRepository(database.locationDoa()) }
    val heartRateRepository by lazy { HeartRateRepository(database.heartRateDao()) }
    val summaryRepository by lazy { SummaryRepository(database.summaryDao()) }

    // Used by workout service
    override fun onCreate() {
        super.onCreate()
        HeartRateDeviceManager.initialize(this)
        initializeWorkoutDataProcessor()
        createNotificationChannel()
    }


    private fun initializeWorkoutDataProcessor() {

        WorkoutDataProcessor.initialize(
            workoutRepository = workoutRepository,
            locationRepository = locationRepository,
            heartRateRepository = heartRateRepository,
            summaryRepository = summaryRepository,
            coroutineScope = applicationScope
        )
        // Has to be blocking because it should initialize before the rest
        // of the application
        runBlocking {
            val activeWorkout = workoutRepository.getUnfinishedWorkout()

            if (activeWorkout != null) {
                val user = userRepository.getUserById(1)
                val summary = summaryRepository.getSummaryForWorkout(activeWorkout.id)

                if (summary == null) {
                    workoutRepository.delete(workout = activeWorkout)
                } else {
                    WorkoutDataProcessor.getInstance().restoreWorkout(user, activeWorkout, summary)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN  //TODO what is our need? NONE, LOW, DEFAULT???
        ).apply {
            description = "Workout service is enable"
            enableLights(false)
            //TODO more channel settings
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }

        val notificationManager = getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(serviceChannel)
    }

}