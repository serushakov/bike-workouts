package io.ushakov.bike_workouts

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.polidea.rxandroidble2.RxBleClient
import io.ushakov.bike_workouts.WorkoutService.Companion.ACTION_STOP
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

        Log.d("MainActivity", ServiceStatus.IS_WORKOUT_SERVICE_RUNNING.toString())
    }


    @Composable
    fun View() {
        val navController = rememberNavController()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        requestPermissions(bluetoothAdapter = bluetoothManager.adapter)

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Main(navController) {
                    if (ServiceStatus.IS_WORKOUT_SERVICE_RUNNING) {
                        stopWorkoutService()
                    } else {
                        startWorkoutService()
                    }
                }
            }
            composable("bluetooth_settings") {
                BluetoothSettings(
                    navController
                )
            }
        }
    }

    private fun stopWorkoutService() {
        val intentStop = Intent(this, WorkoutService::class.java)
        stopService(intentStop)
    }

    private fun startWorkoutService() {
        startService(Intent(this, WorkoutService::class.java))
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