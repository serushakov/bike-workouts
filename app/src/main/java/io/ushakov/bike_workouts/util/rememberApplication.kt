package io.ushakov.bike_workouts.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.ushakov.bike_workouts.WorkoutApplication

@Composable
fun rememberApplication() = LocalContext.current.applicationContext as WorkoutApplication