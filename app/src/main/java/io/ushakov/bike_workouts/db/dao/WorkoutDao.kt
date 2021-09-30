package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.ushakov.bike_workouts.db.entity.UserWorkout
import io.ushakov.bike_workouts.db.entity.Workout

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM WORKOUT")
    fun getAllWorkouts(): LiveData<List<Workout>>

    //@Query("SELECT * FROM WORKOUT WHERE WORKOUT.userId = :userId")
    //fun getUserWorkout(userId: Long): UserWorkout

    //get workoutHeartRate
    //get workoutLocation
    //get workoutSummary

    @Query("DELETE FROM WORKOUT")
    suspend fun deleteAll()

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    suspend fun insert(workout: Workout): Long
}