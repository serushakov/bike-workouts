package io.ushakov.bike_workouts.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.ushakov.bike_workouts.db.dao.*
import io.ushakov.bike_workouts.db.entity.*
import io.ushakov.bike_workouts.util.DatabaseTypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

@Database(entities = [
    (User::class),
    (Workout::class),
    (Location::class),
    (HeartRate::class),
    (Summary::class)], version = 1, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun locationDoa(): LocationDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun summaryDao(): SummaryDao
    // TODO more Dao here

    private class WorkoutDatabaseCallback(
        private val scope: CoroutineScope,
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val userDao = database.userDao()
                    val workoutDao = database.workoutDao()
                    val locationDao = database.locationDoa()
                    val heartRateDao = database.heartRateDao()
                    val summaryDao = database.summaryDao()

                    // This run only once if DB has not be initialized before
                    // TODO Clear after reinstalling App
                    /* userDao.deleteAll()
                     locationDao.deleteAll()
                     workoutDao.deleteAll()
                     heartRateDao.deleteAll()
                     summaryDao.deleteAll()*/


                    // TODO populate new DB after reinstalling App
                    //Adding initial dummy data
                    // Users
                    val userId: Long = userDao.insert(User("ABC", "XYZ"))
                    Log.d("DBG", "User created with Id: $userId")


                    suspend fun createWorkout() {
                        // Workouts
                        // Workout-01
                        val workout = Workout(userId = userId,
                            title = "Workout Title_01",
                            type = 5,
                            startAt = Date(Date().time - 600000))

                        val workoutId: Long = workoutDao.insert(workout)
                        workout.id = workoutId


                        var location_01 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468602)))
                        var location_02 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468604)))
                        var location_03 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468606)))
                        var location_04 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468608)))
                        var location_05 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468610)))
                        var location_06 = locationDao.insert(Location(workoutId,
                            Random.nextDouble(60.000000, 61.000000),
                            Random.nextDouble(23.500000, 24.500000),
                            34.45F,
                            45.6F,
                            Date(1632468612)))


                        var heart_rate_01 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468602)))
                        var heart_rate_02 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468604)))
                        var heart_rate_03 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468606)))
                        var heart_rate_04 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468608)))
                        var heart_rate_05 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468610)))
                        var heart_rate_06 =
                            heartRateDao.insert(HeartRate(workoutId, 50, Date(1632468612)))

                        summaryDao.insert(Summary(workoutId = workoutId, distance = 1.5, kiloCalories = 400))

                        workout.finishAt = Date()
                        workoutDao.update(workout)
                    }

                    repeat(10) {
                        createWorkout()
                    }

                    Log.d("DBG", "Number of Workouts ${workoutDao.getAllWorkouts().value?.size}")

                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope,
        ): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).addCallback(WorkoutDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}