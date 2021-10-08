package io.ushakov.bike_workouts.ui.views

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateOffsetAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.components.InWorkoutInfoRow
import io.ushakov.bike_workouts.ui.components.Info
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.getDifferenceBetweenDates
import kotlinx.coroutines.delay
import java.util.*

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
                    TopRow(lastLocation = locations.lastOrNull(),
                        lastHeartRate = heartRates.lastOrNull(),
                        startTime = workout.startAt)
                    Spacer(Modifier.height(16.dp))
                    Divider()

                    AnimatedContent(targetState = isPaused) { state ->
                        if (state) {
                            InWorkoutInfoRow(
                                Info(text = "200", title = "Kilocalories"),
                                Info(text = "4.3", title = "Kilometres"),
                                Info(text = "13", title = "Elevation"),
                                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
                            )
                        } else {
                            BigDistance(4.3)
                        }
                    }

                    // Holds space for buttons
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
                    onStopClick = onWorkoutStopClick,
                    onPlayClick = { isPaused = false },
                    isPaused,
                    Modifier.alpha(if (hideStopButton) 0f else 1f)
                )

                if (hideStopButton) {
                    PauseButton(modifier = Modifier
                        .align(Alignment.BottomCenter)) {
                        isPaused = !isPaused
                    }
                }

            }

        }
    }
}


@Composable
fun TopRow(lastLocation: Location?, lastHeartRate: HeartRate?, startTime: Date) {
    val speed = if (lastLocation == null) "--.-" else String.format(".1f", lastLocation.speed)
    val heartRate = lastHeartRate?.heartRate?.toString() ?: "--"

    fun calculateTime(): String {
        val diff = getDifferenceBetweenDates(startTime, Date())

        return "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}"
    }

    var time by remember { mutableStateOf(calculateTime()) }

    DisposableEffect(startTime) {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                time = calculateTime()
                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(runnable, 1000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    InWorkoutInfoRow(
        Info(text = speed, title = "kmh"),
        Info(text = heartRate, title = "bpm"),
        Info(text = time, title = "time"),
    )
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

