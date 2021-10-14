package io.ushakov.bike_workouts.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.ushakov.bike_workouts.db.entity.Duration

@Dao
interface DurationDao {

    @Insert
    suspend fun insert(duration: Duration): Long

    @Update
    suspend fun update(duration: Duration): Int

    @Query("SELECT * FROM Duration WHERE id = :durationId")
    suspend fun getById(durationId: Long): Duration
}