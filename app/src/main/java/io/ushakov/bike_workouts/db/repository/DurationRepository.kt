package io.ushakov.bike_workouts.db.repository

import io.ushakov.bike_workouts.db.dao.DurationDao
import io.ushakov.bike_workouts.db.entity.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class DurationRepository(private val durationDao: DurationDao) {

    suspend fun insert(duration: Duration) = withContext(Dispatchers.IO) {
        return@withContext durationDao.insert(duration)
    }

    suspend fun update(duration: Duration) = withContext(Dispatchers.IO) {
        return@withContext durationDao.update(duration)
    }
}