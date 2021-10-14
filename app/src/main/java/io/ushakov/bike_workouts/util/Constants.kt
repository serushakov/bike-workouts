package io.ushakov.bike_workouts.util

import java.util.*

object Constants {
    val HEART_RATE_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    const val ACTION_LOCATION_PROCESS_UPDATES = "io.ushakov.bike_workouts.action" + ".LOCATION_PROCESS_UPDATES"
    const val USER_ID_SHARED_PREFERENCES_KEY = "user_id"
    const val SAVED_DEVICE_SHARED_PREFERENCES_KEY = "device_address"
    //TODO some reason able channel id
    const val CHANNEL_ID = "LOCATION_SERVICE_CHANNEL_ID"
    const val CHANNEL_NAME = "LOCATION_SERVICE_CHANNEL_NAME"
    const val SERVICE_REQUEST_CODE = 12
    const val SERVICE_NOTIFICATION_ID = 34
    const val PACKAGE_NAME ="io.ushakov.bike_workouts"
    const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
    const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
    const val EXTRA_HEART_RATE = "$PACKAGE_NAME.heart-rate"
    const val HEART_DEFAULT_VALUE: Int = 0
    const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000
    const val MINIMUM_WORKOUT_DURATION_S = 20
    const val DEFAULT_WORKOUT_ID: Long = 0
    const val DEFAULT_WORKOUT_TITLE = "DEFAULT_WORKOUT_TITLE"   //TODO replace it with a suitable name later
    const val DEFAULT_WORKOUT_TYPE = "DEFAULT_WORKOUT_TYPE"
    val DEFAULT_WORKOUT_FINISH_TIME = null               // TODO discuss and set some default date
    const val DEFAULT_USER_ID: Long = 0
    const val INITIAL_DISTANCE: Double = 0.0
    const val MINIMUM_AGE = 16
    const val MAXIMUM_AGE = 100
    const val MINIMUM_WEIGHT = 20
    const val MAXIMUM_WEIGHT = 300
}