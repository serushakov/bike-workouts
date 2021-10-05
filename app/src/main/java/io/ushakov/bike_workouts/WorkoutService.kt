package io.ushakov.bike_workouts

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.polidea.rxandroidble2.RxBleClient
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.bike_workouts.util.Constants.CHANNEL_ID
import io.ushakov.bike_workouts.util.Constants.EXTRA_LOCATION
import io.ushakov.bike_workouts.util.Constants.SERVICE_NOTIFICATION_ID
import io.ushakov.bike_workouts.util.Constants.SERVICE_REQUEST_CODE
import io.ushakov.bike_workouts.util.Constants.UPDATE_INTERVAL_IN_MILLISECONDS

//TODO move this service under service folder/module/package
class WorkoutService : Service() {
    //var mainHandler: Handler? = null

    private lateinit var bleClient: RxBleClient
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    companion object {
        const val ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }
    // TODO unused code, Do not know where it came from
/*    private val runnable = object : Runnable {
        override fun run() {
            Log.d("WorkoutService", "ping")

            mainHandler!!.postDelayed(this, 1000)
        }
    }*/

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

        bleClient = RxBleClient.create(this)

        Log.d("WorkoutService", "start")
        ServiceStatus.IS_WORKOUT_SERVICE_RUNNING = true

        //mainHandler = Handler(Looper.getMainLooper())

        //mainHandler!!.postDelayed(runnable, 1000)

        // Workout service Notification
        generateForegroundNotification()

        startLocationUpdates()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        //Not sure if our stuff should be shutdown before calling parent onDestroy().
        stopLocationService()
        super.onDestroy()
        ServiceStatus.IS_WORKOUT_SERVICE_RUNNING = false
        Log.d("WorkoutService", "destroy")

// TODO unused code, Do not know where it came from
        //mainHandler?.removeCallbacks(runnable)
    }

/*    //Notififcation for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123*/

    private fun generateForegroundNotification() {

        val appNotificationIntent = Intent(this, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            this,
            SERVICE_REQUEST_CODE,
            appNotificationIntent,
            0
        )
        //iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        /*if (mNotificationManager == null) {
        mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    assert(mNotificationManager != null)
    mNotificationManager?.createNotificationChannelGroup(
        NotificationChannelGroup("chats_group", "Chats")
    )
    val notificationChannel =
        NotificationChannel("service_channel", "Service Notifications",
            NotificationManager.IMPORTANCE_MIN)
    notificationChannel.enableLights(false)
    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
    mNotificationManager?.createNotificationChannel(notificationChannel)*/

        //val builder = NotificationCompat.Builder(this, "service_channel")
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
            //.color = resources.getColor(R.color.primaryColor, theme)
            //.  color =

        /*val notification1: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location in use")
            .setContentTitle("Workout App is using location")
            .setSmallIcon(com.google.android.gms.location.R.drawable.ic_baseline_location_on_24)
            .setContentIntent(locationPendingIntent)
            .build()*/

        /*builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running")
        .toString())
        .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running")
            .toString())
        .setContentText("Touch to open") //                    , swipe down for more options.
        .setSmallIcon(R.drawable.ic_baseline_add_24)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setWhen(0)
        .setOnlyAlertOnce(true)
        .setContentIntent(pendingIntent)
        .setOngoing(true)
    if (iconNotification != null) {
        builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
    }
    builder.color = resources.getColor(R.color.primaryColor)
    notification = builder.build()*/
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
    fun stopLocationService() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
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