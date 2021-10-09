package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log

class WorkoutDistanceProcessor() {

    private var previousLocation: Location? = null
    private var distance: Double = 0.0

    fun latestDistance(currentLocation: Location): Double {
        if (previousLocation == null) {
            //Log.d("DBG", "previousLocation is null")
            previousLocation = currentLocation
            return 0.0 //or Initial value
        } else {
            //Log.d("DBG", "previousLocation is not null")

            distance += currentLocation.distanceTo(previousLocation).toDouble()
            //Log.d("DBG", "Now distance is $distance")

            previousLocation = currentLocation
            return distance
        }
    }


    fun resetDistance() {
        distance = 0.0
    }
}