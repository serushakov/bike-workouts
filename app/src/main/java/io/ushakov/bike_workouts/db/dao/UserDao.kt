package io.ushakov.bike_workouts.db.dao

import androidx.room.*
import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.UserWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM USER")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM USER where USER.firstName = :firstName AND USER.lastName = :lastName")
    fun getUserByFirstNameAndLastName(firstName: String, lastName: String): User

    @Query("SELECT * FROM USER WHERE USER.id = :userId")
    fun getUserWorkout(userId: Long): Flow<List<UserWorkout>>

    @Query("DELETE FROM USER")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long
}