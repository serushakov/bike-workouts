package io.ushakov.bike_workouts.data_engine

import android.location.Location
import android.util.Log
import android.widget.Toast

class WorkoutDistanceProcessor() {
    private var previousLocation: Location? = null

    fun latestDistance(currentLocation: Location, distanceSoFar: Double): Double {
        return if (previousLocation == null) {
            //Log.d("DBG", "previousLocation is null")
            previousLocation = currentLocation
            0.0 //or Initial value
        } else {
            //Log.d("DBG", "previousLocation is not null")

                val accuracy = currentLocation.accuracy

            val distanceFromLast = currentLocation.distanceTo(previousLocation).toDouble()

            // If the value is under the accuracy, ignore it
            // This filters out noise
            if(distanceFromLast < (accuracy / 2)) return distanceSoFar

            val distance = distanceSoFar + currentLocation.distanceTo(previousLocation).toDouble()

            previousLocation = currentLocation

            distance
            //Log.d("DBG", "Now distance is $distance")
        }
    }
}