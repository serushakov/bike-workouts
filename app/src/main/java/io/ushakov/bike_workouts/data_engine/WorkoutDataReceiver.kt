package io.ushakov.bike_workouts.data_engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.activity.ComponentActivity
import io.ushakov.bike_workouts.MainActivity
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.Constants.EXTRA_HEART_RATE
import io.ushakov.bike_workouts.util.Constants.HEART_DEFAULT_VALUE

class WorkoutDataReceiver : BroadcastReceiver() {
    private var previousHeartRate: Int = HEART_DEFAULT_VALUE
    private var previousLocation: Location? = null

    override fun onReceive(context: Context, intent: Intent) {
        //Log.d("DBG", "Broadcast received")
        //Log.d("DBG", "Intent Content: ${intent.getParcelableExtra<Parcelable>(EXTRA_LOCATION)}")
        val newLocation: Location? = intent.getParcelableExtra(Constants.EXTRA_LOCATION)
        val newHeartRateValue: Int = intent.getIntExtra(EXTRA_HEART_RATE, HEART_DEFAULT_VALUE)
        /*
            Location = Location[
                fused 60.213018,24.922503
                hAcc=5
                et=+15h34m55s11ms
                alt=0.0
                vel=0.0
                bear=90.0
                vAcc=1
                sAcc=1
                bAcc=30 {
                    Bundle[mParcelledData.dataSize=52]
                    }
           ]
        */
        //If heart rate arrives ror location arrives
        if (newHeartRateValue != HEART_DEFAULT_VALUE || newLocation != null) {
            if (newHeartRateValue != HEART_DEFAULT_VALUE) {
                //Log.d("DBG", "DataReceiver: Received heart pulse")
                previousHeartRate = newHeartRateValue
            }
            if (newLocation != null) {
                //Log.d("DBG", "DataReceiver: Received new location")
                previousLocation = newLocation
            }
            writeValues()
        }

        // TODO Wait for both heartRate and location to arrive
        // TODO Create a timestamp
        // TODO Calculate Distance and kCal
        // TODO using Repository, write to DB
    }

    private fun writeValues() {
        if (previousHeartRate != HEART_DEFAULT_VALUE && previousLocation != null) {
            WorkoutDataProcessor.getInstance().processData(previousHeartRate, previousLocation!!)
            resetPreviousValues()
        }
    }

    private fun resetPreviousValues() {
        previousHeartRate = HEART_DEFAULT_VALUE
        previousLocation = null
    }
}