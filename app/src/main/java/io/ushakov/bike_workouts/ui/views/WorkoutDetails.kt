package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.libraries.maps.GoogleMap
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.ui.components.ComposableMap


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
    var map by remember { mutableStateOf<GoogleMap?>(null) }

    ComposableMap(
        Modifier.height(300.dp)
    ) { map = it }
}
