package io.ushakov.bike_workouts.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.ushakov.bike_workouts.db.dao.UserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class WorkoutDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    // TODO more Dao here

    private class WorkoutDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val userDao = database.userDao()
                    // TODO more Doa here

                    userDao.deleteAll()
                    // TODO Clear other Dao here
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