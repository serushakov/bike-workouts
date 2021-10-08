package io.ushakov.bike_workouts.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor
import io.ushakov.bike_workouts.db.entity.Workout

@Composable
fun rememberActiveWorkout(): Workout? {
    val unfinishedWorkout by WorkoutDataProcessor.getInstance().activeWorkout.observeAsState()

    return unfinishedWorkout
}