package io.ushakov.bike_workouts.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.theme.Typography

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
                InfoRow(
                    Row(text = "25.0", title = "kmh"),
                    Row(text = "120", title = "bpm"),
                    Row(text = "0.10.34", title = "time"),
                )
                PauseButton {
                    showButton = !showButton
                }
                if (showButton) {
                    PauseButton {
                        showButton = !showButton
                    }
                }
            }
        }
    }
}

class Row(
    val title: String,
    val text: String,
)

@Composable
fun InfoRow(vararg rows: Row) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        rows.map {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(it.text, style = Typography.h4)
                Spacer(Modifier.height(4.dp))
                Text(it.title.uppercase(),
                    style = Typography.overline,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.SemiBold)
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

