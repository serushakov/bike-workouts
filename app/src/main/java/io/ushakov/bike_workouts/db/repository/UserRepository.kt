package io.ushakov.bike_workouts.db.repository

import io.ushakov.bike_workouts.db.dao.UserDao
import io.ushakov.bike_workouts.db.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    val allUser: Flow<List<User>> = userDao.getAll()

    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        return@withContext userDao.insert(user)
    }
}