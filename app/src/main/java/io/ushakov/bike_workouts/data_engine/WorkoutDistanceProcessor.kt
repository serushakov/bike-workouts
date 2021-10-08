package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log

class WorkoutDistanceProcessor() {

    private var previousLocation: Location? = null

    fun latestDistance(currentLocation: Location): Double {
        if (previousLocation == null) {
            Log.d("DBG", "previousLocation is null")
            previousLocation = currentLocation
            return 0.0 //or Initial value
        } else {
            Log.d("DBG", "previousLocation is not null")

            val newDistance = currentLocation.distanceTo(previousLocation).toDouble()
            Log.d("DBG", "newDistance is $newDistance")

            previousLocation = currentLocation
            return newDistance
        }
    }


    private fun resetPreviousDistance() {

    }
}