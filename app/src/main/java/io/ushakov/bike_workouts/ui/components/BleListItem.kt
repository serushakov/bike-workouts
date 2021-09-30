package io.ushakov.bike_workouts.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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

enum class ButtonStatus {
    DEFAULT,
    PAIRING,
    PAIRED,
    DISABLED
}

@Composable
fun BleListItem(
    deviceName: String,
    buttonStatus: ButtonStatus,
    onPairButtonClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .requiredHeight(48.dp)
    ) {
        Text(text = deviceName)
        Spacer(modifier = Modifier.weight(1f, true))
        when (buttonStatus) {
            ButtonStatus.DEFAULT, ButtonStatus.DISABLED -> Button(onClick = onPairButtonClick,
                enabled = buttonStatus != ButtonStatus.DISABLED) {
                Text(
                    text = stringResource(R.string.device_list_item_button_label)
                )
            }
            ButtonStatus.PAIRING -> Text(
                text = "Pairing..."
            )
            ButtonStatus.PAIRED -> Text(text = "Paired!")
        }
    }
}

@Preview(widthDp = 480, heightDp = 48)
@Composable
fun Preview() {
    BikeWorkoutsTheme {
        Surface() {
            BleListItem(deviceName = "Device name", ButtonStatus.PAIRING) {
            }
        }
    }
}