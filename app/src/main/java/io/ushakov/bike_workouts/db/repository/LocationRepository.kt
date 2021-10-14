package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.LocationDao
import io.ushakov.bike_workouts.db.entity.Location

class LocationRepository(val locationDao: LocationDao) {
    //TODO all function should be suspended

    suspend fun insert(location: Location): Long {
       return locationDao.insert(location)
    }

    fun getLocationsForWorkout(workoutId: Long): LiveData<List<Location>> {
        return locationDao.getLocationsForWorkout(workoutId = workoutId)
    }

    suspend fun getWorkoutAverageSpeed(workoutId: Long) = locationDao.getWorkoutAverageSpeed(workoutId)
}