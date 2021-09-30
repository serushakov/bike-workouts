package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import io.ushakov.bike_workouts.db.entity.WorkoutSummary

@Composable
fun WorkoutList(
    workoutSummaryList: List<WorkoutSummary>,
    onSelected: (WorkoutSummary) -> Unit,
) {
    LazyColumn {
        items(workoutSummaryList) { workoutSummary ->
            WorkoutColumnItem(
                workoutSummary = workoutSummary
            ) { onSelected(workoutSummary) }
        }
    }
}