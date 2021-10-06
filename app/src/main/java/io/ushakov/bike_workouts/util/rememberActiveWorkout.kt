package io.ushakov.bike_workouts.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Workout

@Composable
fun rememberActiveWorkout(): Workout? {
    val application = LocalContext.current.applicationContext as WorkoutApplication
    val unfinishedWorkout by application.workoutRepository.unfinishedWorkout.observeAsState()

    return unfinishedWorkout
}