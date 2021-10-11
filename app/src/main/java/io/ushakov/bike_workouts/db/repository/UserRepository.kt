package io.ushakov.bike_workouts.db.repository

import io.ushakov.bike_workouts.db.dao.UserDao
import io.ushakov.bike_workouts.db.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        return@withContext userDao.insert(user)
    }

    suspend fun getUserById(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserById(userId)
    }
}