package io.ushakov.bike_workouts.db.dao

import androidx.room.Insert
import androidx.room.Update
import io.ushakov.bike_workouts.db.entity.Duration

interface DurationDao {

    @Insert
    suspend fun insert(duration: Duration): Long

    @Update
    suspend fun update(duration: Duration): Int
}