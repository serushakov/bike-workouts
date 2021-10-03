package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.maps.CameraUpdate
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap
import kotlinx.coroutines.delay


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

    Column(Modifier.fillMaxWidth()) {
        WorkoutMapView(locations = locations)
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
