package io.ushakov.bike_workouts.data_engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.Constants.EXTRA_HEART_RATE
import io.ushakov.bike_workouts.util.Constants.HEART_DEFAULT_VALUE

class WorkoutDataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        var newLocation: Location? = intent.getParcelableExtra(Constants.EXTRA_LOCATION)
        var newHeartRateValue: Int = intent.getIntExtra(EXTRA_HEART_RATE, HEART_DEFAULT_VALUE)

        if (newHeartRateValue != HEART_DEFAULT_VALUE) {
            WorkoutDataProcessor.getInstance().processHeartRate(newHeartRateValue)
            newHeartRateValue = HEART_DEFAULT_VALUE
        }

        if (newLocation != null) {
            WorkoutDataProcessor.getInstance().processLocation(newLocation)
            newLocation = null
        }
    }
}