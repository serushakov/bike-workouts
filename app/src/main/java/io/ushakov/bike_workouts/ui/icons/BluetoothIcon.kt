package io.ushakov.bike_workouts.ui.icons

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import io.ushakov.bike_workouts.R

@Composable
fun  BluetoothIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_baseline_bluetooth_24),
        contentDescription = "Bluetooth image",
    )
}