package io.ushakov.bike_workouts

import android.app.Application
import io.ushakov.bike_workouts.db.WorkoutDatabase
import io.ushakov.bike_workouts.db.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


/*
    Stuff that need one instance in application (e.g. Database and Repositories are
    added as a member or Application class). These can be retrieved from the Application
    whenever they're needed, rather than constructed every time.
*/
class WorkoutApplication: Application()  {

    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database by lazy {
        WorkoutDatabase.getDatabase(this, applicationScope)
    }

    val userRepository by lazy { UserRepository(database.userDao()) }
    // TODO more repositories here

}