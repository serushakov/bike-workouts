package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.WorkoutDao
import io.ushakov.bike_workouts.db.entity.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.*

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    //TODO all function should be suspended

    val allWorkout: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    val unfinishedWorkout = workoutDao.getUnfinishedWorkout()

    //TODO this code will move to WorkoutDataProcessor
    /*suspend fun startWorkout(userId: Long) = withContext(Dispatchers.IO) {
        val workout = Workout(
            userId = userId,
            title = "Whatever",
            type = "bike",
            startAt = Date()
        )
        return@withContext workoutDao.insert(workout)
    }*/

    suspend fun insert(workout: Workout) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.insert(workout)
    }

    suspend fun delete(workout: Workout) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.delete(workout)
    }

    suspend fun deleteById(workoutId: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.deleteById(workoutId)
    }

    suspend fun getWorkoutsByUserId(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.getWorkoutsByUserId(userId)
    }

    suspend fun getLastWorkout(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.getLastFinishedWorkoutByUserId(userId)
    }

    fun getCompleteWorkoutById(id: Long) = workoutDao.getCompleteWorkoutById(id)

    suspend fun update(workoutId: Long) = withContext(Dispatchers.IO) {
        val workout = workoutDao.getWorkoutById(workoutId)
        workout.finishAt = Date()
        return@withContext workoutDao.update(workout)
    }

    suspend fun update(workout: Workout) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.update(workout)
    }

    suspend fun update(workoutId: Long, finishTime: Date) = withContext(Dispatchers.IO) {
        return@withContext workoutDao.update(workoutId, finishTime)
    }
}