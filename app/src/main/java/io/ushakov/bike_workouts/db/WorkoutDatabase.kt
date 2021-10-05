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

                    // Workouts
                    // Workout-01
                    val workoutId_01: Long = workoutDao.insert(Workout(userId,"Workout Title_01", "Workout Type_01", Date(), null ))


                    var location_01 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468602)))
                    var location_02 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468604)))
                    var location_03 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468606)))
                    var location_04 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468608)))
                    var location_05 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468610)))
                    var location_06 = locationDao.insert(Location(workoutId_01, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468612)))


                    var heart_rate_01 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468602)))
                    var heart_rate_02 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468604)))
                    var heart_rate_03 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468606)))
                    var heart_rate_04 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468608)))
                    var heart_rate_05 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468610)))
                    var heart_rate_06 = heartRateDao.insert(HeartRate(workoutId_01, 50, Date(1632468612)))

                    summaryDao.insert(Summary(workoutId_01, 1.5, 400))

                    // Workout-02
                    val workoutId_02: Long = workoutDao.insert(Workout(userId,"Workout Title_02", "Workout Type_02", Date(), null ))

                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468602)))
                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468604)))
                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468606)))
                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468608)))
                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468610)))
                    locationDao.insert(Location(workoutId_02, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468612)))

                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468602)))
                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468604)))
                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468606)))
                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468608)))
                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468610)))
                    heartRateDao.insert(HeartRate(workoutId_02, 50, Date(1632468612)))


                    summaryDao.insert(Summary(workoutId_02, 1.5, 400))

                    // Workout-03
                    val workoutId_03: Long = workoutDao.insert(Workout(userId,"Workout Title_03", "Workout Type_03", Date(), null ))


                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468602)))
                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468604)))
                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468606)))
                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468608)))
                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468610)))
                    locationDao.insert(Location(workoutId_03, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468612)))

                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468602)))
                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468604)))
                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468606)))
                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468608)))
                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468610)))
                    heartRateDao.insert(HeartRate(workoutId_03, 50, Date(1632468612)))

                    summaryDao.insert(Summary(workoutId_03, 1.5, 400))


                    // Workout-04
                    val workoutId_04: Long = workoutDao.insert(Workout(userId,"Workout Title_04", "Workout Type_04", Date(), null ))


                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468602)))
                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468604)))
                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468606)))
                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468608)))
                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468610)))
                    locationDao.insert(Location(workoutId_04, Random.nextDouble(60.000000, 61.000000), Random.nextDouble(23.500000, 24.500000), 34.45F, 45.6F, Date(1632468612)))

                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468602)))
                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468604)))
                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468606)))
                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468608)))
                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468610)))
                    heartRateDao.insert(HeartRate(workoutId_04, 50, Date(1632468612)))

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