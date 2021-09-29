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
import io.ushakov.bike_workouts.utilities.DatabaseTypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Database(entities = [
    (User::class),
    (Workout::class),
    (Location::class),
    (HeartRate::class),
    (Summary::class)], version = 1, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class WorkoutDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun locationDoa(): LocationDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun summaryDao(): SummaryDao
    // TODO more Dao here

    private class WorkoutDatabaseCallback(
        private val scope: CoroutineScope
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

                    // TODO Clear other Dao here
                   /* userDao.deleteAll()
                    locationDao.deleteAll()
                    workoutDao.deleteAll()
                    heartRateDao.deleteAll()
                    summaryDao.deleteAll()*/


                    // TODO remember to remove
                    //Adding initial dummy data
                    // Users
                    val userId: Long = userDao.insert(User("ABC", "XYZ"))
                    Log.d("DBG", "User created with Id: $userId")

                    // Workouts
                    // Workout-01
                    val workoutId_01: Long = workoutDao.insert(Workout(userId,"Workout Title_01", "Workout Type_01", Date(), null ))


                    locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_01, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_01, 123.123, 123.123, 34.45F, 45.6F))

                    heartRateDao.insert(HeartRate(workoutId_01, 50))
                    heartRateDao.insert(HeartRate(workoutId_01, 50))
                    heartRateDao.insert(HeartRate(workoutId_01, 50))
                    heartRateDao.insert(HeartRate(workoutId_01, 50))
                    heartRateDao.insert(HeartRate(workoutId_01, 50))

                    summaryDao.insert(Summary(workoutId_01, 1.5, 400))

                    var date = LocalDate.parse("2018-12-31")
                    // Workout-02
                    val workoutId_02: Long = workoutDao.insert(Workout(userId,"Workout Title_02", "Workout Type_02", Date(), null ))

                    locationDao.insert(Location(workoutId_02, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_02, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_02, 123.123, 123.123, 34.45F, 45.6F))

                    heartRateDao.insert(HeartRate(workoutId_02, 50))
                    heartRateDao.insert(HeartRate(workoutId_02, 50))
                    heartRateDao.insert(HeartRate(workoutId_02, 50))
                    heartRateDao.insert(HeartRate(workoutId_02, 50))
                    heartRateDao.insert(HeartRate(workoutId_02, 50))

                    summaryDao.insert(Summary(workoutId_02, 1.5, 400))

                    // Workout-03
                    val workoutId_03: Long = workoutDao.insert(Workout(userId,"Workout Title_03", "Workout Type_03", Date(), null ))


                    locationDao.insert(Location(workoutId_03, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_03, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_03, 123.123, 123.123, 34.45F, 45.6F))

                    heartRateDao.insert(HeartRate(workoutId_03, 50))
                    heartRateDao.insert(HeartRate(workoutId_03, 50))
                    heartRateDao.insert(HeartRate(workoutId_03, 50))
                    heartRateDao.insert(HeartRate(workoutId_03, 50))
                    heartRateDao.insert(HeartRate(workoutId_03, 50))

                    summaryDao.insert(Summary(workoutId_03, 1.5, 400))


                    // Workout-04
                    val workoutId_04: Long = workoutDao.insert(Workout(userId,"Workout Title_04", "Workout Type_04", Date(), null ))


                    locationDao.insert(Location(workoutId_04, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_04, 123.123, 123.123, 34.45F, 45.6F))
                    locationDao.insert(Location(workoutId_04, 123.123, 123.123, 34.45F, 45.6F))

                    heartRateDao.insert(HeartRate(workoutId_04, 50))
                    heartRateDao.insert(HeartRate(workoutId_04, 50))
                    heartRateDao.insert(HeartRate(workoutId_04, 50))
                    heartRateDao.insert(HeartRate(workoutId_04, 50))
                    heartRateDao.insert(HeartRate(workoutId_04, 50))

                    summaryDao.insert(Summary(workoutId_04, 1.5, 400))

                    Log.d("DBG", "Number of Workouts ${workoutDao.getAllWorkouts().value?.size}")

                }
            }
        }
    }

    companion object{
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
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