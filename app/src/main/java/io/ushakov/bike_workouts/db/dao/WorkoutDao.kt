package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.db.entity.WorkoutDuration
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import java.util.*

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM WORKOUT")
    fun getAllWorkouts(): LiveData<List<Workout>>

    @Query("SELECT * FROM workout WHERE workout.id = :id")
    suspend fun getWorkoutById(id: Long): Workout

    @Query("SELECT * FROM workout WHERE workout.finishAt is null LIMIT 1")
    fun getLiveUnfinishedWorkout(): LiveData<Workout>

    @Query("SELECT * FROM workout WHERE workout.finishAt is null LIMIT 1")
    suspend fun getUnfinishedWorkout(): Workout?

    @Delete
    fun delete(workout: Workout)

    @Query("DELETE FROM workout WHERE id = :workoutId")
    suspend fun deleteById(workoutId: Long): Int

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

    //Used by main activity to show list of workout displaying summary of each workout
    @Transaction
    @Query("SELECT * FROM WORKOUT WHERE WORKOUT.userId = :userId")
    suspend fun getWorkoutsByUserId(userId: Long): List<WorkoutSummary>

    //Used by main activity to show list of workout displaying summary of each workout
    @Transaction
    @Query("SELECT * FROM WORKOUT WHERE WORKOUT.userId = :userId AND finishAt is not null ORDER BY id DESC LIMIT 1")
    suspend fun getLastFinishedWorkoutByUserId(userId: Long): WorkoutSummary

    @Transaction
    @Query("SELECT * FROM workout WHERE workout.id = :id")
    fun getCompleteWorkoutById(id: Long): LiveData<WorkoutComplete>

    @Update
    suspend fun update(workout: Workout): Int

    @Query("UPDATE WORKOUT SET finishAt = :finishTime WHERE id = :workoutId")
    suspend fun update(workoutId: Long, finishTime: Date): Int

    @Transaction
    @Query("SELECT * FROM workout WHERE workout.id = :workoutId")
    suspend fun getWorkoutDurations(workoutId: Long): WorkoutDuration
}