package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.ushakov.bike_workouts.db.entity.HeartRate

@Dao
interface HeartRateDao {

    @Query("SELECT * FROM HEARTRATE")
    fun getAll(): LiveData<List<HeartRate>>

    @Query("DELETE FROM HEARTRATE")
    suspend fun deleteAll()

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    suspend fun insert(heartRate: HeartRate): Long

    @Query("SELECT * FROM heartrate WHERE heartRate.workoutId = :workoutId")
    fun getForWorkout(workoutId: Long): LiveData<List<HeartRate>>
}