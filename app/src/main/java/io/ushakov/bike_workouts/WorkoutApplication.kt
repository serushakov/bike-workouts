package io.ushakov.bike_workouts

import android.app.Application
import io.ushakov.bike_workouts.db.WorkoutDatabase
import io.ushakov.bike_workouts.db.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WorkoutApplication: Application()  {

    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy {
        WorkoutDatabase.getDatabase(this, applicationScope)
    }

    val userRepository by lazy { UserRepository(database.userDao()) }
    val workoutRepository by lazy { WorkoutRepository(database.workoutDao()) }
    val locationRepository by lazy { LocationRepository(database.locationDoa()) }
    val heartRateRepository by lazy { HeartRateRepository(database.heartRateDao()) }
    val summaryRepository by lazy { SummaryRepository(database.summaryDao()) }

}