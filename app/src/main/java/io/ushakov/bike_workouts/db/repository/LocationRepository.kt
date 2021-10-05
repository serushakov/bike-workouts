package io.ushakov.bike_workouts.db.repository

import androidx.lifecycle.LiveData
import io.ushakov.bike_workouts.db.dao.LocationDao
import io.ushakov.bike_workouts.db.entity.Location

class LocationRepository(val locationDoa: LocationDao) {
    //TODO all function should be suspended

    suspend fun insert(location: Location): Long {
       return locationDoa.insert(location)
    }

    fun getLocationsForWorkout(workoutId: Long): LiveData<List<Location>> {
        return locationDoa.getLocationsForWorkout(workoutId = workoutId)
    }
}