package io.ushakov.bike_workouts.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.ushakov.bike_workouts.db.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM USER")
    fun getAll(): LiveData<List<User>>

    // Used when app starts for the first time
    @Query("SELECT * FROM USER where USER.firstName = :firstName AND USER.lastName = :lastName")
    suspend fun getUserByFirstNameAndLastName(firstName: String, lastName: String): User

    //@Query("SELECT user.id,summary.workoutId, workout.startAt, summary.distance, summary.kiloCalories From User INNER JOIN WORKOUT ON WORKOUT.userId = USER.id INNER JOIN SUMMARY ON summary.workoutId = Workout.id WHERE User.id = :userId")
    //suspend fun getUserWorkouts(userId: Long): List<UserWorkoutListItem>
    //@Query("SELECT * FROM USER WHERE USER.id = :userId")
    //suspend fun getUserWorkouts(userId: Long): List<UserWorkout>

    @Query("DELETE FROM USER")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM USER WHERE id = :userId")
    suspend fun getUserById(userId: Long): User
}