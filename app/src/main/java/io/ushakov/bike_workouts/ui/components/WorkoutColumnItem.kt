package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Black4
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.distanceToKm
import java.text.DateFormat
import java.util.*

@Composable
fun WorkoutColumnItem(
    date: Date,
    distance: Double,
    kcal: Int,
    onClick: () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()

    Card(Modifier
        .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = if (darkTheme) 2.dp else 0.dp) {
        Row(
            modifier =
            Modifier
                .background(if (darkTheme) Color.Transparent else Black4)
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                content = {
                    BicycleIcon()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (darkTheme) PrimaryOverlayDark else PrimaryOverlay),
                contentAlignment = Alignment.Center
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(2F),
                content = {
                    DateText(date = date)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${
                            String.format("%.2f",
                                distanceToKm(distance))
                        }km, ${kcal}kcal",
                        style = Typography.h6
                    )
                })
            Spacer(modifier = Modifier.size(16.dp))
            ArrowForward()
        }
    }
}

@Composable
fun DateText(date: Date) {
    val formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)

    Text(
        text = formattedDate.toString(),
        style = Typography.caption
    )
}

@Preview()
@Composable
fun WorkoutColumnItemPreview() {
    WorkoutColumnItem(date = Date(), distance = 12.0, kcal = 500) {

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
            .size(24.dp)
    )
}

@Composable
fun ArrowForward() {
    Icon(
        painter = painterResource(R.drawable.ic_baseline_arrow_forward_ios_24),
        contentDescription = "Right Arrow image",
        tint = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .size(24.dp)
    )
}