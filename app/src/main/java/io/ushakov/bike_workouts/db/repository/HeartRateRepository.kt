package io.ushakov.bike_workouts.db.repository

import io.ushakov.bike_workouts.db.dao.HeartRateDao
import io.ushakov.bike_workouts.db.entity.HeartRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HeartRateRepository(private val heartRateDao: HeartRateDao) {
    //TODO all function should be suspended
    suspend fun insert(heartRate: HeartRate) = withContext(Dispatchers.IO) {
        return@withContext heartRateDao.insert(heartRate)
    }

    fun getHeartRatesForWorkout(workoutId: Long) = heartRateDao.getForWorkout(workoutId)
}