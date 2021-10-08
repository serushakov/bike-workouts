package io.ushakov.bike_workouts.ui.views.in_workout

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.views.in_workout.components.WorkoutNumbers
import kotlinx.coroutines.delay

enum class UiState {
    WORKOUT, PAUSED
}

@Composable
fun InWorkout(workoutComplete: WorkoutComplete, onWorkoutStopClick: () -> Unit) {
    val (
        workout,
        heartRates,
        locations,
        summary,
    ) = workoutComplete

    if (workout == null || heartRates == null || locations == null) return

    var uiState by remember { mutableStateOf(UiState.WORKOUT) }

    var hideStopButton by remember { mutableStateOf(uiState != UiState.PAUSED) }


    LaunchedEffect(uiState) {
        hideStopButton = if (uiState == UiState.PAUSED) {
            false
        } else {
            delay(200)
            true
        }
    }


    Column(Modifier.fillMaxSize()) {
        ComposableMap(onInit = {}, modifier = Modifier
            .fillMaxWidth()
            .weight(1f))
        Box(
            Modifier.fillMaxWidth()
        ) {
            Surface(
                Modifier
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                WorkoutNumbers(workout, summary, heartRates, locations, uiState)
            }

            Box(modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)) {

                StopPlayButtons(
                    onStopClick = onWorkoutStopClick,
                    onPlayClick = { uiState = UiState.WORKOUT },
                    uiState == UiState.PAUSED,
                    Modifier
                        .alpha(if (hideStopButton) 0f else 1f)
                        .align(Alignment.BottomCenter)
                )

                if (hideStopButton) {
                    PauseButton(modifier = Modifier
                        .align(Alignment.BottomCenter)) {
                        uiState = UiState.PAUSED
                    }
                }

            }
        }
    }
}


@Composable
fun PauseButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick,
        modifier.size(80.dp)
    ) {
        Icon(Icons.Default.Pause, "Pause", modifier = Modifier.size(42.dp))
    }
}

@Composable
fun StopPlayButtons(
    onStopClick: () -> Unit,
    onPlayClick: () -> Unit,
    show: Boolean,
    modifier: Modifier = Modifier,
) {
    val offset by animateOffsetAsState(targetValue = if (show) Offset(80f, 0f) else Offset.Zero)


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        FloatingActionButton(
            onClick = onStopClick,
            modifier
                .size(80.dp)
                .offset(x = (offset.x * -1).dp)
                .align(Alignment.Center),
            backgroundColor = Color.Black,
        ) {
            Icon(Icons.Default.Stop,
                "Stop",
                modifier = Modifier.size(42.dp),
                tint = Color.White)
        }


        FloatingActionButton(
            onPlayClick,
            Modifier
                .size(80.dp)
                .offset(x = offset.x.dp)
                .align(Alignment.Center)

        ) {
            Icon(Icons.Default.PlayArrow, "Play", modifier = Modifier.size(42.dp))
        }
    }
}

