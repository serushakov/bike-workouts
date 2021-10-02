package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.WorkoutSummary

@Composable
fun WorkoutList(
    workoutSummaryList: List<WorkoutSummary>,
    onSelected: (WorkoutSummary) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        itemsIndexed(workoutSummaryList.filter { it.summary != null && it.workout != null }) { index, workoutSummary ->
            WorkoutColumnItem(
                workoutSummary.workout!!.startAt,
                workoutSummary.summary!!.distance,
                workoutSummary.summary!!.kiloCalories
            ) { onSelected(workoutSummary) }

            if (index != workoutSummaryList.size - 1) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}