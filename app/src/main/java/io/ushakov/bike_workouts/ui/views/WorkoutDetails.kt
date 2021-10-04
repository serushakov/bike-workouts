package io.ushakov.bike_workouts.ui.views

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.*
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.components.SectionTitleText
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.getDifferenceBetweenDates
import java.lang.Float.min
import java.util.*

@Composable
fun WorkoutDetails(workoutId: Long?) {
    val application = LocalContext.current.applicationContext as WorkoutApplication
    var workoutComplete by remember { mutableStateOf<WorkoutComplete?>(null) }

    LaunchedEffect(workoutId) {
        workoutComplete =
            if (workoutId != null) application.workoutRepository.getCompleteWorkoutById(workoutId) else null
    }

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
            WorkoutMapView(locations = locations)
        }

        Surface(
            Modifier
                .offset(y = (-8).dp)
                .clip(RoundedCornerShape(16.dp))
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
                CaloriesAverageHeartRateRow(
                    kcal = summary.kiloCalories,
                    heartRates = heartRates
                )
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()

                InfoRow(titleStart = "Duration",
                    valueStart = "0:45:34",
                    titleEnd = "Distance",
                    valueEnd = "12.45km")
                Divider()
            }
        }
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
        valueEnd = "${String.format("%.2f", distance)}km")
}

@Composable
fun CaloriesAverageHeartRateRow(
    kcal: Int,
    heartRates: List<HeartRate>
) {
    val averageHr = heartRates.map { it.heartRate }.average().toInt()

    InfoRow(titleStart = "🔥Burned Calories",
        valueStart = "${kcal}kcal",
        titleEnd = "❤️Average Heartrate",
        valueEnd = "${averageHr}bpm")
}

@Composable
fun WorkoutMapView(locations: List<Location>, modifier: Modifier = Modifier) {
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    DisposableEffect(locations, googleMap) {
        val map = googleMap ?: return@DisposableEffect onDispose { }

        val polyLine = map.addPolyline(
            PolylineOptions()
                .addAll(locations.map { LatLng(it.latitude, it.longitude) })
                .width(5f)
                .color(android.graphics.Color.RED)
                .endCap(RoundCap())
                .startCap(RoundCap())
        )

        val bounds = LatLngBounds.builder()
        locations.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }

        map.setLatLngBoundsForCameraTarget(bounds.build())
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20))

        onDispose {
            polyLine.remove()
        }
    }

    Log.d("WorkoutMapView", "render")
    ComposableMap(
        Modifier
            .height(300.dp)
            .composed { modifier }
    ) {
        if (googleMap == null) googleMap = it
    }
}
