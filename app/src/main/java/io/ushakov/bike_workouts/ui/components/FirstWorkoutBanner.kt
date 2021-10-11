package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.ui.theme.Typography


@Composable
fun FirstWorkoutBanner() {
    Card(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = 16.dp,
    ) {
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(all = 8.dp)) {
            Text("To start your first workout, press the START button below 😊",
                textAlign = TextAlign.Center,
                style = Typography.body1,
                modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}