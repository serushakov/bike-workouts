package io.ushakov.bike_workouts.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R

@Composable
fun  BicycleIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_baseline_directions_bike_24),
        contentDescription = "Bicycle image",
        tint = Color.Blue,
        modifier = Modifier
            .size(36.dp)
    )
}