package io.ushakov.bike_workouts.ui.views.in_workout.components

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.distanceToKm
import io.ushakov.bike_workouts.util.mpsToKmh
import java.util.*

enum class InfoItem {
    Calories, Distance, Elevation, Heartrate, Speed, Time
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkoutNumbers(
    workout: Workout,
    summary: Summary?,
    heartRates: List<HeartRate>,
    locations: List<Location>,
    isWorkoutActive: Boolean,
) {
    var rowItems by remember {
        mutableStateOf(listOf(InfoItem.Speed,
            InfoItem.Heartrate,
            InfoItem.Time))
    }
    var centerItem by remember { mutableStateOf(InfoItem.Distance) }

    Column(Modifier
        .padding(16.dp)
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            rowItems.forEachIndexed { index, infoItem ->
                val (text, title) = formatInfoItem(
                    infoItem,
                    workout,
                    summary,
                    heartRates,
                    locations,
                )

                InfoRowItem(text = text,
                    title = title,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // Swap centered item with clicked one
                            val clickedInfoItem = rowItems[index]

                            rowItems = rowItems.mapIndexed { mapIndex, infoItem ->
                                if (mapIndex == index) {
                                    centerItem
                                } else {
                                    infoItem
                                }
                            }

                            centerItem = clickedInfoItem

                        })
            }
        }
        Spacer(Modifier.height(16.dp))

        Divider()

        AnimatedContent(targetState = isWorkoutActive) { isActive ->
            if (isActive) {
                val (text, title) = formatInfoItem(
                    centerItem,
                    workout,
                    summary,
                    heartRates,
                    locations,
                )

                CenterInfoItem(text = text, title = title)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp)
                ) {
                    listOf(InfoItem.Calories,
                        centerItem,
                        InfoItem.Elevation).forEach { infoItem ->

                        val (text, title) = formatInfoItem(
                            infoItem,
                            workout,
                            summary,
                            heartRates,
                            locations,
                        )

                        InfoRowItem(text, title, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Holds space for buttons
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun formatInfoItem(
    infoItem: InfoItem,
    workout: Workout,
    summary: Summary?,
    heartRates: List<HeartRate>,
    locations: List<Location>,
): Pair<String, String> {
    return when (infoItem) {
        InfoItem.Distance -> {
            val distance = if (summary != null) String.format("%.2f",
                distanceToKm(summary.distance)) else stringResource(R.string.in_workout__info_row__distance_fallback)
            (distance to stringResource(R.string.in_workout__info_row__distance__title))
        }
        InfoItem.Heartrate -> {
            val heartrate =
                heartRates.lastOrNull()?.heartRate?.toString()
                    ?: stringResource(
                        R.string.in_workout__info_row__heartrate__fallback)

            heartrate to stringResource(R.string.in_workout__info_row__heartrate__title)
        }
        InfoItem.Speed -> {
            val speed = locations.lastOrNull()?.speed

            val formattedSpeed = if (speed != null) String.format("%.1f", mpsToKmh(speed))
            else stringResource(R.string.in_workout__info_row__speed__fallback)

            formattedSpeed to stringResource(R.string.in_workout__info_row__speed__title)
        }
        InfoItem.Time -> {
            val time = rememberWorkoutTime(workout.startAt)

            time to stringResource(R.string.in_workout__info_row__time__title)
        }
        InfoItem.Calories -> {
            val calories = summary?.kiloCalories?.toString()
                ?: stringResource(R.string.in_workout__info_row__calories__fallback)

            calories to stringResource(R.string.in_workout__info_row__calories__title)
        }
        InfoItem.Elevation -> {
            val elevation = locations.lastOrNull()?.elevation

            val formattedElevation = if (elevation != null) String.format("%.1f", elevation)
            else stringResource(R.string.in_workout__info_row__elevation__fallback)

            formattedElevation to stringResource(R.string.in_workout__info_row__elevation__title)
        }
    }
}

@Composable
fun CenterInfoItem(title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = Typography.h1
        )
        Spacer(Modifier.height(4.dp))
        SmallTitle(text = title)
    }
}

@Composable
fun rememberWorkoutTime(startTime: Date): String {
    var time by remember { mutableStateOf(WorkoutDataProcessor.getInstance().calculateTime()) }

    DisposableEffect(startTime) {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                time = WorkoutDataProcessor.getInstance().calculateTime()
                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(runnable, 1000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    return time
}


@Composable
fun InfoRowItem(
    text: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, style = Typography.h4)
        Spacer(Modifier.height(4.dp))
        SmallTitle(title)
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