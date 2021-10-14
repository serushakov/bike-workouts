package io.ushakov.bike_workouts.data_engine

import android.location.Location

class WorkoutDistanceProcessor() {
    private var previousLocation: Location? = null

    fun latestDistance(currentLocation: Location, distanceSoFar: Double): Double {
        return if (previousLocation == null) {
            previousLocation = currentLocation
            0.0
        } else {

                val accuracy = currentLocation.accuracy

            val distanceFromLast = currentLocation.distanceTo(previousLocation).toDouble()

            // If the value is under the accuracy, ignore it
            // This filters out noise
            if(distanceFromLast < (accuracy / 2)) return distanceSoFar

            val distance = distanceSoFar + currentLocation.distanceTo(previousLocation).toDouble()

            previousLocation = currentLocation

            distance
        }
    }
}