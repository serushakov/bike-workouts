package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.WorkoutDao
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.UserWorkout
import io.ushakov.bike_workouts.db.entity.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    //TODO all function should be suspended

    val allWorkout: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: Workout) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.insert(workout)
    }

    /*fun getWorkoutByUser(userId: Long): UserWorkout {
        return workoutDao.getUserWorkout(userId)
    }*/
}