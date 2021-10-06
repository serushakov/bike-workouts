package io.ushakov.bike_workouts.ui.views

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.color.MaterialColors
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.theme.Typography
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InWorkout(workoutComplete: WorkoutComplete, onWorkoutStopClick: () -> Unit) {
    val (
        workout,
        heartRates,
        locations,
    ) = workoutComplete

    if (workout == null || heartRates == null || locations == null) return

    var isPaused by remember { mutableStateOf(false) }

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

                Column(Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    InfoRow(
                        Row(text = "25.0", title = "kmh"),
                        Row(text = "120", title = "bpm"),
                        Row(text = "0.10.34", title = "time"),
                    )
                    Spacer(Modifier.height(16.dp))
                    Divider()

                    AnimatedContent(targetState = isPaused) { state ->
                        if (state) {
                            InfoRow(
                                Row(text = "200", title = "Kilocalories"),
                                Row(text = "4.3", title = "Kilometres"),
                                Row(text = "13", title = "Elevation"),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        } else {
                            BigDistance(4.3)
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }


            var hideStopButton by remember { mutableStateOf(!isPaused) }


            LaunchedEffect(isPaused) {
                hideStopButton = if (isPaused) {
                    false
                } else {
                    delay(200)
                    true
                }
            }

            Box(modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)) {

                StopPlayButtons(
                    onStopClick = {},
                    onStopLongClick = onWorkoutStopClick,
                    onPlayClick = { isPaused = false },
                    isPaused,
                    Modifier.alpha(if (hideStopButton) 0f else 1f)
                )

                PauseButton(modifier = Modifier
                    .alpha(if (hideStopButton) 1f else 0f)
                    .align(Alignment.BottomCenter)) {
                    isPaused = !isPaused
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
fun InfoRow(vararg rows: Row, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        rows.map {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(it.text, style = Typography.h4)
                Spacer(Modifier.height(4.dp))
                SmallTitle(it.title)
            }
        }
    }
}

@Composable
fun BigDistance(distance: Double, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%.1f", distance),
            style = Typography.h1
        )
        Spacer(Modifier.height(4.dp))
        SmallTitle(text = "Kilometres")
    }
}

@Composable
fun SmallTitle(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(),
        modifier,
        style = Typography.overline,
        color = MaterialTheme.colors.primary,
        fontWeight = FontWeight.SemiBold)
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
    onStopLongClick: () -> Unit,
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
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onStopLongClick() })
                }
                .align(Alignment.Center),
            backgroundColor = Color.Black,
            contentColor = MaterialTheme.colors.contentColorFor(Color.Black),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(42.dp))
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

