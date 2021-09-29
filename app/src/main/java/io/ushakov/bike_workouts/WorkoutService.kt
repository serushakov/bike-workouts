package io.ushakov.bike_workouts

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class WorkoutService : Service() {
    var mainHandler: Handler?= null

    companion object {
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val runnable = object : Runnable {
        override fun run() {
            Log.d("WorkoutService", "ping")

            mainHandler!!.postDelayed(this, 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP, ignoreCase = true)
        ) {
            stopSelf()
        }

        Log.d("WorkoutService", "start")
        ServiceStatus.IS_WORKOUT_SERVICE_RUNNING = true

        mainHandler = Handler(Looper.getMainLooper())

        mainHandler!!.postDelayed(runnable, 1000)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceStatus.IS_WORKOUT_SERVICE_RUNNING = false
        Log.d("WorkoutService", "destroy")

        mainHandler?.removeCallbacks(runnable)
    }
}