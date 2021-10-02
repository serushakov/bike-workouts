package io.ushakov.bike_workouts.ui.views

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun Main(navController: NavController) {
    Scaffold(
        topBar = { MainAppBar(navController) }
    ) {

    }
}

@Composable
fun MainAppBar(navController: NavController) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {  }) {
                Icon(Icons.Default.AccountCircle, "Account")
            }
        },
        title = {},
        actions = {
            IconButton(onClick = { navController.navigate("workout_history") }) {
                Icon(Icons.Default.History, "Workout history")
            }
            IconButton(onClick = { navController.navigate("bluetooth_settings") }) {
                Icon(Icons.Default.Bluetooth, "Bluetooth")
            }
        },
        backgroundColor = Color.White,
        elevation = 0.dp
    )
}