package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.WorkoutDao
import io.ushakov.bike_workouts.db.entity.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    //TODO all function should be suspended

    val allWorkout: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    val unfinishedWorkout = workoutDao.getUnfinishedWorkout()

    suspend fun startWorkout(userId: Long) = withContext(Dispatchers.IO) {
        val workout = Workout(
            userId = userId,
            title = "Whatever",
            type = "bike",
            startAt = Date()
        )
        return@withContext workoutDao.insert(workout)
    }

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

    suspend fun finishWorkout(id: Long) = withContext(Dispatchers.IO) {
        val workout = workoutDao.getWorkoutById(id)
        workout.finishAt = Date()
        return@withContext workoutDao.update(workout)
    }
}