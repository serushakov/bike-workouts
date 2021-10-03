package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.ui.theme.Blue700
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.myapplication.ui.theme.Typography

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = Typography.overline,
        color = if(isSystemInDarkTheme()) Blue700 else Blue800,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(8.dp))
}