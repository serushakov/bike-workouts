package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.UserWorkouts

@Dao
interface UserDao {
    @Query("SELECT * FROM USER")
    fun getAll(): LiveData<List<User>>

    @Query("DELETE FROM USER")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM USER WHERE id = :userId")
    suspend fun getUserById(userId: Long): User

    @Transaction
    @Query("SELECT * FROM USER WHERE id = :userId")
    suspend fun getUserWithWorkouts(userId: Long): UserWorkouts?
}