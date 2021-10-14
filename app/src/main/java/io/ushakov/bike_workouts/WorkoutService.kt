package io.ushakov.bike_workouts

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import io.reactivex.disposables.Disposable
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.bike_workouts.util.Constants.CHANNEL_ID
import io.ushakov.bike_workouts.util.Constants.EXTRA_HEART_RATE
import io.ushakov.bike_workouts.util.Constants.EXTRA_LOCATION
import io.ushakov.bike_workouts.util.Constants.SERVICE_NOTIFICATION_ID
import io.ushakov.bike_workouts.util.Constants.SERVICE_REQUEST_CODE
import io.ushakov.bike_workouts.util.Constants.UPDATE_INTERVAL_IN_MILLISECONDS

//TODO move this service under service folder/module/package
class WorkoutService : Service() {
    //var mainHandler: Handler? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private var heartrateNotificationsDisposable: Disposable? = null

    companion object {
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initiateLocationRequest()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                ACTION_STOP, ignoreCase = true)
        ) {
            stopSelf()
        }

        startHeartrateUpdates()
        generateForegroundNotification()
        startLocationUpdates()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeListeners()
        //Not sure if our stuff should be shutdown before calling parent onDestroy().
        super.onDestroy()
    }


    private fun generateForegroundNotification() {

        val appNotificationIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            this,
            SERVICE_REQUEST_CODE,
            appNotificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${resources.getString(R.string.app_name)} service is running")
            .setTicker("${resources.getString(R.string.app_name)} service is running")
            .setContentText("Touch to open")
            .setSmallIcon(R.drawable.ic_baseline_pedal_bike_24)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setWhen(0)
            .setOnlyAlertOnce(true)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(resources.getColor(R.color.primaryColor, theme))
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    //TODO Add permission or use PermissionUtility
    //App is working, if it crush or does not work, change location permission to always On
    // FIXME Adjust permissions
    //@SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //TODO Ask permission
            //return false
            return
        }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startHeartrateUpdates() {
        heartrateNotificationsDisposable = HeartRateDeviceManager.getInstance().subscribe { heartrate ->

            Log.d("Heartrate", heartrate.toString())
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_HEART_RATE, heartrate)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    //Location Callback
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult!!)
            // Notify anyone listening for broadcasts about the new location.
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, locationResult.lastLocation)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    //Not private, may be used from outside
    fun removeListeners() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        heartrateNotificationsDisposable?.dispose()
        removeNotification()
    }

    private fun removeNotification() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun initiateLocationRequest() {
        locationRequest = LocationRequest
            .create()
            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

}