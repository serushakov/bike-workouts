package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.R
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme

@Composable
fun BleListItem(
    deviceName: String,
    onPairButtonClick: () -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = deviceName)
        Spacer(modifier = Modifier.weight(1f, true))
        Button(onClick = onPairButtonClick) {
            Text(
                text = stringResource(R.string.device_list_item_button_label)
            )
        }
    }
}

@Preview(widthDp = 480, heightDp = 48)
@Composable
fun Preview() {
    BikeWorkoutsTheme {
        Surface() {
            BleListItem(deviceName = "Device name") {
            }
        }
    }
}