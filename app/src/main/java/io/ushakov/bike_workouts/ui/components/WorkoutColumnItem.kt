package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.ui.icons.ArrowForward
import io.ushakov.bike_workouts.ui.icons.BicycleIcon

@Composable
fun WorkoutColumnItem(
    workoutSummary: WorkoutSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        content = {
            Box(
                content = {
                    BicycleIcon()
                },
                modifier = Modifier
                    .size(80.dp)
                    .border(width = 1.2.dp, Color.Blue, shape = CircleShape),
                contentAlignment = Alignment.Center
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(
                modifier = Modifier.weight(2F),
                content = {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "${workoutSummary.workout?.startAt?: "data missing"}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "${workoutSummary.summary?.distance?: "Missing"} Km, ${workoutSummary.summary?.kiloCalories?: "Missing"} kCal",
                        color = Color.Black,
                        fontSize = 14.6.sp
                    )
                })
            Spacer(modifier = Modifier.size(16.dp))
            ArrowForward()
        })
}