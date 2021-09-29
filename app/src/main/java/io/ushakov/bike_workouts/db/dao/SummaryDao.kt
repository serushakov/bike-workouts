package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.entity.Summary

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SummaryDao {

    @Query("SELECT * FROM SUMMARY")
    fun getAll(): LiveData<List<Summary>>

    @Query("DELETE FROM SUMMARY")
    suspend fun deleteAll()

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    suspend fun insert(summary: Summary): Long
}