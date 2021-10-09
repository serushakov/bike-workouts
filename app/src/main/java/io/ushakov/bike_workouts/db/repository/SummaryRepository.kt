package io.ushakov.bike_workouts.db.repository

import io.ushakov.bike_workouts.db.dao.SummaryDao
import io.ushakov.bike_workouts.db.entity.Summary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SummaryRepository(private val summaryDao: SummaryDao) {
    //TODO all function should be suspended
    suspend fun insert(summary: Summary) = withContext(Dispatchers.IO) {
        return@withContext summaryDao.insert(summary)
    }

    suspend fun update(summary: Summary) = withContext(Dispatchers.IO) {
        return@withContext summaryDao.update(summary)
    }
}