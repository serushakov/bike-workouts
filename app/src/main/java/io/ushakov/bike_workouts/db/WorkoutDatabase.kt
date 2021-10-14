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
    (Summary::class),
    (Duration::class)], version = 1, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class WorkoutDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun locationDoa(): LocationDao
    abstract fun heartRateDao(): HeartRateDao
    abstract fun summaryDao(): SummaryDao
    abstract fun durationDao(): DurationDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(
            context: Context,
        ): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}