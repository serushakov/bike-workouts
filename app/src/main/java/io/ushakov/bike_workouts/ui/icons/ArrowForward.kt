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
fun ArrowForward() {
    Icon(
        painter = painterResource(R.drawable.ic_baseline_arrow_forward_ios_24),
        contentDescription = "Right Arrow image",
        tint = Color.Black,
        modifier = Modifier
            .size(38.dp)
    )
}