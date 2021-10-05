package io.ushakov.bike_workouts.data_engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.Constants.EXTRA_HEART_RATE
import io.ushakov.bike_workouts.util.Constants.HEART_DEFAULT_VALUE

class DataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //Log.d("DBG", "Broadcast received")
        //Log.d("DBG", "Intent Content: ${intent.getParcelableExtra<Parcelable>(EXTRA_LOCATION)}")
        val location: Location? = intent.getParcelableExtra(Constants.EXTRA_LOCATION)
        val heartRateValue: Int = intent.getIntExtra(
            EXTRA_HEART_RATE,
            HEART_DEFAULT_VALUE
        )
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
        if (location != null) {
            Log.d("DBG", "DataReceiver:Location = $location")
        }
        //Log.d("DBG", "DataReceiver: Heart BPM = $heartRateValue")
        //Log.d("DBG", "Intent: = ${intent.action}")

        // TODO Wait for both heartRate and location to arrive
        // TODO Create a timestamp
        // TODO Calculate Distance and kCal
        // TODO using Repository, write to DB
    }
}