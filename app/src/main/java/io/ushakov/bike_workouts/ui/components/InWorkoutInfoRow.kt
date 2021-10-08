package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.ui.views.SmallTitle

class Info(
    val title: String,
    val text: String,
)

@Composable
fun InWorkoutInfoRow(vararg infos: Info, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        infos.map {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(it.text, style = Typography.h4)
                Spacer(Modifier.height(4.dp))
                SmallTitle(it.title)
            }
        }
    }
}