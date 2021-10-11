package io.ushakov.bike_workouts.ui.views.first_time_setup.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.ui.theme.Typography

@Composable
fun Layout(modifier: Modifier = Modifier, titleText: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(modifier
            .padding(horizontal = 30.dp)
            .fillMaxWidth()
            .weight(1f)) {
            Text(
                text = titleText,
                style = Typography.h2,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Column(Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .padding(bottom = 30.dp)
        ) {
            content()
        }
    }
}
