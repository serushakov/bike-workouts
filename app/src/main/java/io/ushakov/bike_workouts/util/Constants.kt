package io.ushakov.bike_workouts.util

import java.util.*

object Constants {
    val HEART_RATE_CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    const val ACTION_LOCATION_PROCESS_UPDATES = "io.ushakov.bike_workouts.action" + ".LOCATION_PROCESS_UPDATES"
    //TODO some reason able channel id
    const val CHANNEL_ID = "LOCATION_SERVICE_CHANNEL_ID"
    const val CHANNEL_NAME = "LOCATION_SERVICE_CHANNEL_NAME"
    const val SERVICE_REQUEST_CODE = 12
    const val SERVICE_NOTIFICATION_ID = 34
    private const val PACKAGE_NAME ="io.ushakov.bike_workouts"
    const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
    const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
    const val EXTRA_HEART_RATE = "$PACKAGE_NAME.heart-rate"
    const val HEART_DEFAULT_VALUE: Int = 53
    const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
}