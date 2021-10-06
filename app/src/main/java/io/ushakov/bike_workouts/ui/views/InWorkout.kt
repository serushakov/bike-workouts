package io.ushakov.bike_workouts.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap

@Composable
fun InWorkout(workoutComplete: WorkoutComplete, onWorkoutStopClick: () -> Unit) {
    val (
        workout,
        heartRates,
        locations,
    ) = workoutComplete

    if (workout == null || heartRates == null || locations == null) return

    var showButton by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ComposableMap(onInit = {}, modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        Surface(
            Modifier
                .offset(y = (-16).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Column(Modifier
                .padding(16.dp)
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                PauseButton {
                    showButton = !showButton
                }
                if(showButton) {
                    PauseButton {
                        showButton = !showButton
                    }
                }
            }
        }
    }
}

@Composable
fun PauseButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick,
        Modifier.size(80.dp)
    ) {
        Icon(Icons.Default.Pause, "Pause", modifier = Modifier.size(42.dp))
    }
}