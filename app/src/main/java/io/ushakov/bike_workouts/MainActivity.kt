package io.ushakov.bike_workouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.ui.views.BluetoothSettings
import io.ushakov.bike_workouts.ui.views.Main
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BikeWorkoutsTheme {
                View()
            }
        }
    }


    @Composable
    fun View() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {
            composable("main") { Main(navController) }
            composable("bluetooth_settings") { BluetoothSettings(navController) }
        }
    }


}