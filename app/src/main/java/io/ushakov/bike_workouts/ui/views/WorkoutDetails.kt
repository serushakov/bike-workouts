package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.components.SectionTitleText
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
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

    Surface(Modifier.fillMaxSize()) {
        Column {
            WorkoutMapView(locations = locations)
            Header(workout)
            Divider()
            InfoRow(titleStart = "Duration",
                valueStart = "0:45:34",
                titleEnd = "Distance",
                valueEnd = "12.45km")
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
    val formattedStart = android.text.format.DateFormat.format("hh:mm", start)
    val formattedEnd =
        if (end == null) null else android.text.format.DateFormat.format("hh:mm", end)

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
fun WorkoutMapView(locations: List<Location>) {
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
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 10))

        onDispose {
            polyLine.remove()
        }
    }

    ComposableMap(
        Modifier.height(300.dp)
    ) {
        googleMap = it
    }
}
