package io.ushakov.bike_workouts.util

import io.ushakov.bike_workouts.db.entity.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

data class DateDifference(
    val seconds: Int,
    val minutes: Int,
    val hours: Int,
    val days: Int,
) {
    companion object {
        fun fromDuration(duration: Long): DateDifference {
            val seconds: Long = duration / 1000
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
    }
}

fun mpsToKmh(mps: Float) = mps * 3.6
fun mpsToKmh(mps: Double) = mps * 3.6

fun distanceToKm(distance: Double) = distance / 1000

fun <T> debounce(
    waitMs: Long = 300L,
    coroutineScope: CoroutineScope,
    destinationFunction: (T) -> Unit,
): (T) -> Unit {
    var debounceJob: Job? = null
    return { param: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(waitMs)
            destinationFunction(param)
        }
    }
}

fun calculateWorkoutDuration(durations: List<Duration>): Long = durations.fold(0) { acc, duration ->
    val stopTime = duration.stopAt ?: Date()

    acc + (stopTime.time - duration.startAt.time)
}