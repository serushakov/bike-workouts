package io.ushakov.bike_workouts

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.ui.views.BluetoothSettings
import io.ushakov.bike_workouts.ui.views.Main
import io.ushakov.bike_workouts.view_models.BluetoothSettingsViewModel
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

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        requestPermissions(bluetoothAdapter = bluetoothManager.adapter)

        NavHost(navController = navController, startDestination = "main") {
            composable("main") { Main(navController) }
            composable("bluetooth_settings") {
                BluetoothSettings(
                    navController,
                    BluetoothSettingsViewModel(application, bluetoothManager.adapter)
                )
            }
        }
    }


    private fun requestPermissions(bluetoothAdapter: BluetoothAdapter) {
        if (!bluetoothAdapter.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
}