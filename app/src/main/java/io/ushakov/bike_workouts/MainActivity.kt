package io.ushakov.bike_workouts

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.ushakov.bike_workouts.ui.views.BluetoothSettings
import io.ushakov.bike_workouts.ui.views.Main
import io.ushakov.bike_workouts.ui.views.WorkoutDetails
import io.ushakov.bike_workouts.ui.views.WorkoutHistory
import io.ushakov.bike_workouts.ui.theme.BikeWorkoutsTheme

/*
TODO Setup activity calls DB and gets user and it then pass UserId here, which should be store in shared preferences
*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HeartRateDeviceManager.initialize(applicationContext)

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

        val (pairedDevice, setPairedDevice) = remember {
            mutableStateOf(HeartRateDeviceManager.getInstance().getDevice())
        }

        val (pairingDeviceAddress, setPairingDeviceAddress) = remember {
            val sharedPreferences: SharedPreferences =
                applicationContext.getSharedPreferences("shared", MODE_PRIVATE)

            val savedDeviceAddress = sharedPreferences.getString("device_address", null)

            mutableStateOf(savedDeviceAddress)
        }

        val (isPairing, setIsPairing) = remember {
            mutableStateOf(false)
        }

        DisposableEffect(pairingDeviceAddress) {
            if (pairingDeviceAddress == null) return@DisposableEffect onDispose { }

            val disposable =
                HeartRateDeviceManager
                    .getInstance()
                    .setupDevice(pairingDeviceAddress, {
                        setIsPairing(false)
                        setPairedDevice(it)
                        saveDeviceAddress(pairingDeviceAddress)
                    }) {
                        Log.d("Connection error", it.localizedMessage?.toString() ?: "")
                    }

            onDispose {
                disposable.dispose()
            }
        }

        DisposableEffect(pairedDevice) {
            if (pairedDevice == null) {
                HeartRateDeviceManager.getInstance().forgetDevice()

                return@DisposableEffect onDispose { }
            }

            val disposable = HeartRateDeviceManager.getInstance().subscribe {
                Log.d("HEARTRATE", it.toString())
            }
            onDispose {
                disposable.dispose()
            }
        }

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Main(navController, 1)
            }
            composable("workout_history") {
                WorkoutHistory(navController, 1)
            }
            composable("bluetooth_settings") {
                BluetoothSettings(
                    navController,
                    isPairing = isPairing,
                    pairingDeviceAddress = pairingDeviceAddress,
                    pairedDevice = pairedDevice,
                    onDevicePair = { address ->
                        setIsPairing(true)
                        setPairingDeviceAddress(address)
                    }
                ) {

                }
            }
            composable("workout_details/{workoutId}",
                arguments = listOf(navArgument("workoutId") {
                    type = NavType.LongType
                })) { backStackEntry ->
                WorkoutDetails(navController, workoutId = backStackEntry.arguments?.getLong("workoutId"))
            }
        }
    }

    private fun saveDeviceAddress(address: String) {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("device_address", address)
        editor.apply()
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