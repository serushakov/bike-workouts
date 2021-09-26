package io.ushakov.bike_workouts.db.dao

import androidx.room.*
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.UserWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE user.id = :userId")
    fun getUserWorkout(userId: Long): Flow<List<UserWorkout>>

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long
}