package io.ushakov.bike_workouts.util

import java.util.*

data class DateDifference(
    val seconds: Int,
    val minutes: Int,
    val hours: Int,
    val days: Int,
)

fun getDifferenceBetweenDates(date1: Date, date2: Date): DateDifference {
    val millisecondsDiff = if (date1.after(date2)) {
        date1.time - date2.time
    } else {
        date2.time - date1.time
    }

    val seconds: Long = millisecondsDiff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    val days = (hours / 24).toInt()

    return DateDifference(
        hours = (hours % 24).toInt(),
        minutes = (minutes % 60).toInt(),
        seconds = (seconds % 60).toInt(),
        days = days,
    )

}

fun mpsToKmh(mps: Float) = mps * 3.6
fun mpsToKmh(mps: Double) = mps * 3.6