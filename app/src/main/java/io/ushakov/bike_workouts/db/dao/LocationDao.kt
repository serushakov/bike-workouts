package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.ushakov.bike_workouts.db.entity.Location

@Dao
interface LocationDao {

    @Query("SELECT * FROM LOCATION")
    fun getAll(): LiveData<List<Location>>

    @Query("DELETE FROM LOCATION")
    suspend fun deleteAll()

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    suspend fun insert(location: Location): Long
}