package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.WorkoutDao
import io.ushakov.bike_workouts.db.entity.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    //TODO all function should be suspended

    val allWorkout: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: Workout) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.insert(workout)
    }

    suspend fun getWorkoutsByUserId(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.getWorkoutsByUserId(userId)
    }

    suspend fun getLastWorkout(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.getLastWorkoutByUserId(userId)
    }

    suspend fun getCompleteWorkoutById(id: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.getCompleteWorkoutById(id)
    }
}