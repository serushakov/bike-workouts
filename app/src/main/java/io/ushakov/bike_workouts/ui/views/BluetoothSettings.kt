package io.ushakov.bike_workouts.ui.views

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.R
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme


@Composable
fun BluetoothSettings(navController: NavController) {
    Scaffold(
        topBar = { BluetoothSettingsAppBar(navController) }
    ) {

    }
}

@Composable
fun BluetoothSettingsAppBar(navController: NavController) {
    TopAppBar(
        title = { Text(stringResource(R.string.bluetooth_setup_title)) },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.navigation_back))
            }
        },
    )
}

@Preview(widthDp = 480, heightDp = 840)
@Composable
internal fun BluetoothSettingsPreview() {
    BikeWorkoutsTheme {
        BluetoothSettings(rememberNavController())
    }
}