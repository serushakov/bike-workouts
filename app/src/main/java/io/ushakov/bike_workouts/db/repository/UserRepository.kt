package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.UserDao
import io.ushakov.bike_workouts.db.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    //TODO all function should be suspended

    val allUser: LiveData<List<User>> = userDao.getAll()

    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        return@withContext userDao.insert(user)
    }

    suspend fun getUserById(userId: Long) = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserById(userId)
    }

    suspend fun getUserByFirstNameAndLastName(firstName: String, lastName: String): User {
        return userDao.getUserByFirstNameAndLastName(firstName, lastName)
    }
}