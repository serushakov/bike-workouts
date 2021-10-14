package io.ushakov.bike_workouts.ui.views.workout_details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Duration
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.ui.components.SectionTitleText
import io.ushakov.bike_workouts.ui.components.WorkoutMap
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.DateDifference
import io.ushakov.bike_workouts.util.calculateWorkoutDuration
import io.ushakov.bike_workouts.util.distanceToKm
import io.ushakov.bike_workouts.util.mpsToKmh
import java.lang.Float.min
import java.util.*

@Composable
fun WorkoutDetails(workoutId: Long?, onBackPress: () -> Unit) {
    val application = LocalContext.current.applicationContext as WorkoutApplication
    val workoutComplete by application.workoutRepository.getCompleteWorkoutById(workoutId ?: return)
        .observeAsState()

    val locations = workoutComplete?.locations
    val heartRates = workoutComplete?.heartRates
    val summary = workoutComplete?.summary
    val workout = workoutComplete?.workout
    val durations = workoutComplete?.duration

    if (
        locations == null ||
        heartRates == null ||
        summary == null ||
        workout == null
    ) return

    val scrollState = rememberScrollState()


    BackHandler {
        onBackPress()
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(Modifier.graphicsLayer {
            alpha = min(1f, 1 - (scrollState.value / 800f))
            translationY = scrollState.value * 0.5f
        }) {
            WorkoutMap(locations = locations, userLocation = null, modifier = Modifier
                .height(300.dp))
            BackButton(onBackPress)
        }

        Surface(
            Modifier
                .offset(y = (-8).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            elevation = 4.dp
        ) {
            Column {

                Header(workout)
                Divider()

                DurationDistanceRow(
                    durations ?: listOf(),
                    distance = summary.distance
                )
                Divider()

                CaloriesHeartRateRow(
                    kcal = summary.kiloCalories,
                    heartRates = heartRates
                )
                Divider()

                ElevationSpeedRow(locations)
                Divider()

                Column(Modifier.padding(all = 16.dp)) {
                    SectionTitleText(stringResource(R.string.workout_details__heartrate_title))
                    HeartRateGraph(heartRates,
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp))
                }
                Divider()

                Column(Modifier.padding(all = 16.dp)) {
                    SectionTitleText(stringResource(R.string.workout_details__elevation_title))
                    ElevationGraph(locations,
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackButton(onBackPress: () -> Unit) {

    Surface(
        shape = CircleShape,
        onClick = {
            onBackPress()
        },
        role = Role.Button,
        elevation = 12.dp,
        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary,
        modifier = Modifier.offset(x = 16.dp, y = 16.dp)
    ) {
        Icon(Icons.Default.ArrowBack,
            stringResource(R.string.back_button_label),
            Modifier.padding(all = 8.dp))
    }
}

@Composable
fun Header(workout: Workout) {
    val darkTheme = isSystemInDarkTheme()

    Row(Modifier
        .fillMaxWidth()
        .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            content = {
                BicycleIcon()
            },
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (darkTheme) PrimaryOverlayDark else PrimaryOverlay),
            contentAlignment = Alignment.Center
        )
        Spacer(Modifier.width(16.dp))
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.offset(y = (-5).dp),
                text = stringResource(R.string.workout_details__cycling_title),
                style = Typography.h4,
            )
            TimeInterval(start = workout.startAt,
                end = workout.finishAt,
                Modifier.offset(y = (-5).dp))
        }
    }
}

@Composable
fun TimeInterval(start: Date, end: Date?, modifier: Modifier = Modifier) {
    val formattedStart = android.text.format.DateFormat.format("HH:mm", start)
    val formattedEnd =
        if (end == null) null else android.text.format.DateFormat.format("HH:mm", end)

    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            modifier = modifier,
            text = if (formattedEnd == null) "$formattedStart" else "$formattedStart - $formattedEnd",
            style = Typography.subtitle1,
        )
    }
}

@Composable
fun BicycleIcon() {
    val darkTheme = isSystemInDarkTheme()

    Icon(
        Icons.Default.DirectionsBike,
        contentDescription = stringResource(R.string.workout_details__bicycle_icon),
        tint = if (darkTheme) MaterialTheme.colors.onSurface else Blue800,
        modifier = Modifier
            .size(40.dp)
    )
}

@Composable
fun InfoRow(
    titleStart: String,
    valueStart: String,
    titleEnd: String,
    valueEnd: String,
) {
    Row(modifier = Modifier.padding(all = 16.dp)) {
        Column(
            Modifier
                .padding(end = 16.dp)
                .weight(1f)
        ) {
            SectionTitleText(titleStart)
            Text(
                text = valueStart,
                style = Typography.h5
            )
        }
        Column(
            Modifier.weight(1f)
        ) {
            SectionTitleText(titleEnd)
            Text(
                text = valueEnd,
                style = Typography.h5
            )
        }
    }
}

@Composable
fun DurationDistanceRow(
    durations: List<Duration>,
    distance: Double,
) {
    val duration: Long = calculateWorkoutDuration(durations)
    val diff = DateDifference.fromDuration(duration)


    InfoRow(titleStart = stringResource(R.string.workout_details__duration_title),
        valueStart = "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}",
        titleEnd = stringResource(R.string.workout_details__distance_title),
        valueEnd = stringResource(R.string.workout_details__distance_value, distanceToKm(distance)))
}

@Composable
fun CaloriesHeartRateRow(
    kcal: Int,
    heartRates: List<HeartRate>,
) {
    val averageHr = heartRates.map { it.heartRate }.average().toInt()

    InfoRow(titleStart = stringResource(R.string.workout_details__calories_title),
        valueStart = stringResource(R.string.workout_details__calories_value, kcal),
        titleEnd = stringResource(R.string.workout_details__average_hr_title),
        valueEnd = stringResource(R.string.workout_details__heartrate_value, averageHr))
}

@Composable
fun ElevationSpeedRow(
    locations: List<Location>,
) {
    val elevations = locations.map { it.elevation.toInt() }
    val maxElevation = elevations.maxOrNull()
    val minElevation = elevations.minOrNull()

    val averageSpeed = mpsToKmh(locations.map { it.speed }.average())

    InfoRow(titleStart = stringResource(R.string.workout_details__elevation_updown_title),
        valueStart = stringResource(R.string.workout_details__elevation_value,
            maxElevation ?: 0,
            minElevation ?: 0),
        titleEnd = stringResource(R.string.workout_details__speed_title),
        valueEnd = stringResource(R.string.workout_details__speed_value, averageSpeed))
}


