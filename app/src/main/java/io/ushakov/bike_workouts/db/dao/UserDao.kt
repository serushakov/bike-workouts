package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.UserWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM USER")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM USER where USER.firstName = :firstName AND USER.lastName = :lastName")
    suspend fun getUserByFirstNameAndLastName(firstName: String, lastName: String): User

    @Query("SELECT * FROM USER WHERE USER.id = :userId")
    suspend fun getUserWorkout(userId: Long): UserWorkout

    @Query("DELETE FROM USER")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
}