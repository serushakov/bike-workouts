package io.ushakov.bike_workouts

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor
import io.ushakov.bike_workouts.data_engine.WorkoutDataReceiver
import io.ushakov.bike_workouts.ui.theme.BikeWorkoutsTheme
import io.ushakov.bike_workouts.ui.views.*
import io.ushakov.bike_workouts.ui.views.in_workout.InWorkout
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.bike_workouts.util.Constants.SAVED_DEVICE_SHARED_PREFERENCES_KEY
import io.ushakov.bike_workouts.util.rememberActiveWorkout
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.*
import java.util.*

/*
TODO Setup activity calls DB and gets user and it then pass UserId here, which should be store in shared preferences
*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedDeviceAddress = getSavedDeviceAddress()
        if (savedDeviceAddress != null) {
            HeartRateDeviceManager.getInstance().setupDevice(savedDeviceAddress) {}
        }


        //Initialize Broadcast receiver
        val workoutDataReceiver = WorkoutDataReceiver()
        workoutDataReceiver.let {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(it, IntentFilter(ACTION_BROADCAST))
        }

        setContent {
            BikeWorkoutsTheme {
                View()
            }
        }
    }

    override fun onDestroy() {
        stopWorkoutService()
        //TODO Remember to remove it. Dummy HR readings are running in this scope.
        CoroutineScope(Dispatchers.IO).cancel("MainActivity is closed")
        super.onDestroy()
    }


    @Composable
    fun View() {
        val navController = rememberNavController()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val application = rememberApplication()

        requestPermissions(bluetoothAdapter = bluetoothManager.adapter)

        NavigateToUnfinishedWorkout(navController)
        StartWorkoutService()

        val pairedDevice by HeartRateDeviceManager.getInstance().device.observeAsState()

        LaunchedEffect(pairedDevice) {
            saveDeviceAddress(pairedDevice?.macAddress ?: return@LaunchedEffect)
        }

        val isPairing by HeartRateDeviceManager.getInstance().isPairing.observeAsState()
        val isDeviceConnected by HeartRateDeviceManager.getInstance().isConnected.observeAsState()

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Main(navController, 1, isDeviceConnected ?: false) { startWorkout() }
            }
            composable("workout_history") {
                WorkoutHistory(navController, 1)
            }
            composable("bluetooth_settings") {
                BluetoothSettings(
                    navController,
                    isPairing = isPairing ?: false,
                    device = pairedDevice,
                    onDevicePair = { address ->
                        HeartRateDeviceManager.getInstance().setupDevice(address) {}
                    }
                ) {
                    HeartRateDeviceManager.getInstance().forgetDevice()
                    forgetSavedDevice()
                }
            }
            composable("workout_details/{workoutId}",
                arguments = listOf(navArgument("workoutId") {
                    type = NavType.LongType
                })) { backStackEntry ->
                WorkoutDetails(navController,
                    workoutId = backStackEntry.arguments?.getLong("workoutId"))
            }
            composable("in_workout") {
                val activeWorkout = rememberActiveWorkout() ?: return@composable

                val locations by
                application.locationRepository.getLocationsForWorkout(activeWorkout.id)
                    .observeAsState(listOf())

                val heartRates by
                application.heartRateRepository.getHeartRatesForWorkout(activeWorkout.id)
                    .observeAsState(listOf())

                val summary by
                application.summaryRepository.getLiveSummaryForWorkout(activeWorkout.id)
                    .observeAsState()

                InWorkout(workout = activeWorkout,
                    locations = locations,
                    heartRates = heartRates,
                    summary = summary,
                    onWorkoutPauseClick = { WorkoutDataProcessor.getInstance().pauseWorkout() },
                    onWorkoutResumeClick = { WorkoutDataProcessor.getInstance().resumeWorkout() },
                    onWorkoutStopClick = { WorkoutDataProcessor.getInstance().stopWorkout() }
                )
            }
        }
    }


    private fun startWorkout() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = (application as WorkoutApplication).userRepository.getUserById(1)

            WorkoutDataProcessor.getInstance()
                .createWorkout(user, "workout title", 5)
        }
    }

    @Composable
    private fun NavigateToUnfinishedWorkout(navController: NavController) {
        val activeWorkout = rememberActiveWorkout()

        LaunchedEffect(key1 = activeWorkout?.id) {
            if (activeWorkout == null) {
                if (navController.currentDestination?.route == "in_workout") {
                    navController.popBackStack()
                    navController.navigate("main")
                }
            } else {
                navController.popBackStack()
                navController.navigate("in_workout")
            }
        }
    }

    @Composable
    private fun StartWorkoutService() {
        val activeWorkout = rememberActiveWorkout()

        LaunchedEffect(key1 = activeWorkout?.id) {
            if (activeWorkout == null) {
                Log.d("DBG", "Something stops activity here")

                stopWorkoutService()
            } else {
                Log.d("DBG", "Something starts activity here")

                startWorkoutService()
            }
        }
    }

    private fun saveDeviceAddress(address: String) {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SAVED_DEVICE_SHARED_PREFERENCES_KEY, address)
        editor.apply()
    }

    private fun getSavedDeviceAddress(): String? {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)
        return sharedPreferences.getString(SAVED_DEVICE_SHARED_PREFERENCES_KEY, null)
    }

    private fun forgetSavedDevice() {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SAVED_DEVICE_SHARED_PREFERENCES_KEY, null)
        editor.apply()
    }

    private fun stopWorkoutService() {
        Log.d("DBG", "Stopping Workout Service.......")
        val intentStop = Intent(this, WorkoutService::class.java)
        stopService(intentStop)
        Log.d("DBG", "Workout Service stopped")

    }

    private fun startWorkoutService() {
        //startService(Intent(this, WorkoutService::class.java))
        Log.d("DBG", "Starting Workout Service.......")

        val workoutServiceIntent = Intent(this, WorkoutService::class.java)
        workoutServiceIntent.putExtra("SOME_EXTRA_INPUT", "Todo See later")
        ContextCompat.startForegroundService(this, workoutServiceIntent)
        Log.d("DBG", "Workout Service started")

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