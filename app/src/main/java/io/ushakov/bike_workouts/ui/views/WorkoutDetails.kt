package io.ushakov.bike_workouts.ui.views

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.HeartRate
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.ui.components.SectionTitleText
import io.ushakov.bike_workouts.ui.components.WorkoutMap
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.distanceToKm
import io.ushakov.bike_workouts.util.getDifferenceBetweenDates
import io.ushakov.bike_workouts.util.mpsToKmh
import java.lang.Float.min
import java.util.*

@Composable
fun WorkoutDetails(navController: NavController, workoutId: Long?) {
    val application = LocalContext.current.applicationContext as WorkoutApplication
    val workoutComplete by application.workoutRepository.getCompleteWorkoutById(workoutId ?: return)
        .observeAsState()

    val locations = workoutComplete?.locations
    val heartRates = workoutComplete?.heartRates
    val summary = workoutComplete?.summary
    val workout = workoutComplete?.workout

    if (
        locations == null ||
        heartRates == null ||
        summary == null ||
        workout == null
    ) return

    val scrollState = rememberScrollState()


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
            BackButton(navController)
        }

        Surface(
            Modifier
                .offset(y = (-8).dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            Column {

                Header(workout)
                Divider()

                DurationDistanceRow(
                    start = workout.startAt,
                    end = workout.finishAt ?: Date(),
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

                Row(Modifier.padding(all = 16.dp)) {
                    SectionTitleText("Heart rate")
                    Spacer(Modifier.height(100.dp))
                }
                Divider()

                Row(Modifier.padding(all = 16.dp)) {
                    SectionTitleText("Elevation")
                    Spacer(Modifier.height(100.dp))
                }
                Divider()

                Row(Modifier.padding(all = 16.dp)) {
                    SectionTitleText("Something else")
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BackButton(navController: NavController) {

    Surface(
        shape = CircleShape,
        onClick = {
            navController.popBackStack()
        },
        role = Role.Button,
        elevation = 12.dp,
        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary,
        modifier = Modifier.offset(x = 16.dp, y = 16.dp)
    ) {
        Icon(Icons.Default.ArrowBack, "Back", Modifier.padding(all = 8.dp))
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
                text = "Cycling",
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
        contentDescription = "Bicycle image",
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
    start: Date,
    end: Date,
    distance: Double,
) {
    val diff = getDifferenceBetweenDates(start, end)

    InfoRow(titleStart = "⏱Duration",
        valueStart = "${diff.hours}:${
            diff.minutes.toString().padStart(2, '0')
        }:${diff.seconds.toString().padStart(2, '0')}",
        titleEnd = "🗺Distance",
        valueEnd = "${String.format("%.2f", distanceToKm(distance))}km")
}

@Composable
fun CaloriesHeartRateRow(
    kcal: Int,
    heartRates: List<HeartRate>,
) {
    val averageHr = heartRates.map { it.heartRate }.average().toInt()

    InfoRow(titleStart = "🔥Burned Calories",
        valueStart = "${kcal}kcal",
        titleEnd = "❤️Average Heartrate",
        valueEnd = "${averageHr}bpm")
}

@Composable
fun ElevationSpeedRow(
    locations: List<Location>,
) {
    val elevations = locations.map { it.elevation.toInt() }
    val maxElevation = elevations.maxOrNull()
    val minElevation = elevations.minOrNull()

    InfoRow(titleStart = "🏔️Elevation",
        valueStart = "${maxElevation}🔺 ${minElevation}🔻",
        titleEnd = "🚴‍️Average speed",
        valueEnd = "${String.format("%.1f", mpsToKmh(locations.map { it.speed }.average()))}km/h")
}