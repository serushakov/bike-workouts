package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun InWorkout(workoutComplete: WorkoutComplete, onWorkoutStopClick: () -> Unit) {
    val application = rememberApplication()

    val (
        workout,
        heartRates,
        locations,
    ) = workoutComplete

    if (workout == null || heartRates == null || locations == null) return

    val scope = rememberCoroutineScope()

    Surface {
        Column(Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onWorkoutStopClick) {
                Text("STOP")
            }
        }
    }
}