package io.ushakov.bike_workouts

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor
import io.ushakov.bike_workouts.db.WorkoutDatabase
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.repository.*
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.Constants.CHANNEL_ID
import io.ushakov.bike_workouts.util.Constants.CHANNEL_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

class WorkoutApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy {
        WorkoutDatabase.getDatabase(this)
    }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao()) }
    val locationRepository by lazy { LocationRepository(database.locationDoa()) }
    val heartRateRepository by lazy { HeartRateRepository(database.heartRateDao()) }
    val summaryRepository by lazy { SummaryRepository(database.summaryDao()) }
    val durationRepository by lazy { DurationRepository(database.durationDao()) }

    var user: User? = null

    // Used by workout service
    override fun onCreate() {
        super.onCreate()
        HeartRateDeviceManager.initialize(this)
        // Has to be blocking because it should initialize before the rest
        // of the application

        val userId = getSavedUserId()

        if (userId != null) {
            runBlocking {
                user = userRepository.getUserById(userId)
                if (user == null) return@runBlocking

                initializeWorkoutDataProcessor(user!!)
            }
        }
        createNotificationChannel()
    }


    private suspend fun initializeWorkoutDataProcessor(user: User) {
        WorkoutDataProcessor.initialize(
            workoutRepository = workoutRepository,
            locationRepository = locationRepository,
            heartRateRepository = heartRateRepository,
            summaryRepository = summaryRepository,
            durationRepository = durationRepository,
            coroutineScope = applicationScope
        )

        val activeWorkout = workoutRepository.getUnfinishedWorkout()

        if (activeWorkout != null) {

            val summary = summaryRepository.getSummaryForWorkout(activeWorkout.id)

            if (summary == null) {
                workoutRepository.delete(workout = activeWorkout)
            } else {
                WorkoutDataProcessor.getInstance().restoreWorkout(user, activeWorkout, summary)
            }
        }

    }

    private fun getSavedUserId(): Long? {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)

        val savedId = sharedPreferences.getLong(Constants.USER_ID_SHARED_PREFERENCES_KEY, -1)

        if (savedId == (-1).toLong()) return null
        return savedId
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