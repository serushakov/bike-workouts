package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.ushakov.bike_workouts.db.entity.User

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
}