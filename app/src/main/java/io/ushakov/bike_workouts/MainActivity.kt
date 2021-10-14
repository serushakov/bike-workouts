package io.ushakov.bike_workouts

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import io.ushakov.bike_workouts.data_engine.WorkoutDataProcessor
import io.ushakov.bike_workouts.data_engine.WorkoutDataReceiver
import io.ushakov.bike_workouts.ui.theme.BikeWorkoutsTheme
import io.ushakov.bike_workouts.ui.views.*
import io.ushakov.bike_workouts.ui.views.first_time_setup.FirstTimeSetup
import io.ushakov.bike_workouts.ui.views.first_time_setup.components.Permissions
import io.ushakov.bike_workouts.ui.views.in_workout.InWorkout
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.bike_workouts.util.Constants.SAVED_DEVICE_SHARED_PREFERENCES_KEY
import io.ushakov.bike_workouts.util.Constants.USER_ID_SHARED_PREFERENCES_KEY
import io.ushakov.bike_workouts.util.rememberActiveWorkout
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.*
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedDeviceAddress = getSavedDeviceAddress()
        if (savedDeviceAddress != null) {
            HeartRateDeviceManager.getInstance().setupDevice(savedDeviceAddress) {}
        }

        // Views will avoid keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val workoutDataReceiver = WorkoutDataReceiver()
        workoutDataReceiver.let {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(it, IntentFilter(ACTION_BROADCAST))
        }

        setContent {
            BikeWorkoutsTheme {
                Root()
            }
        }
    }

    override fun onDestroy() {
        stopWorkoutService()
        WorkoutDataProcessor.getInstance().pauseWorkout()
        super.onDestroy()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun Root() {
        val application = rememberApplication()
        var isFirstTimeSetupDone by remember { mutableStateOf(application.user != null) }
        var userId by remember { mutableStateOf(application.user?.id) }

        val locationPermissionState =
            rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        var arePermissionsReallyGranted by remember { mutableStateOf(locationPermissionState.hasPermission) }


        if (!isFirstTimeSetupDone || userId == null) {
            FirstTimeSetup { newUserId ->
                saveUserId(newUserId)
                userId = newUserId
                isFirstTimeSetupDone = true
                application.initializeWorkoutDataProcessor(newUserId)
            }
        } else if (!locationPermissionState.hasPermission && !arePermissionsReallyGranted) {
            // Re-request permissions if app does not have them any more
            Permissions(reRequestingPermissions = true) {
                arePermissionsReallyGranted = true
            }
        } else {
            View(userId = userId!!)
        }
    }


    @Composable
    fun View(userId: Long) {
        val navController = rememberNavController()
        val application = rememberApplication()
        val scope = rememberCoroutineScope()

        NavigateToUnfinishedWorkout(navController)
        StartWorkoutService()

        val isDeviceConnected by HeartRateDeviceManager.getInstance().isConnected.observeAsState()

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Main(navController, userId, isDeviceConnected ?: false) { startWorkout() }
            }
            composable("workout_history") {
                WorkoutHistory(navController, userId)
            }
            composable("bluetooth_settings") {
                val isPairing by HeartRateDeviceManager.getInstance().isPairing.observeAsState()
                val pairedDevice by HeartRateDeviceManager.getInstance().device.observeAsState()

                LaunchedEffect(pairedDevice) {
                    saveDeviceAddress(pairedDevice?.macAddress ?: return@LaunchedEffect)
                }

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
                    onWorkoutResumeClick = {
                        WorkoutDataProcessor.getInstance().resumeWorkout()
                    },
                    onWorkoutStopClick = {
                        scope.launch {
                            val workoutId = WorkoutDataProcessor.getInstance().stopWorkout()
                            if (workoutId != null) {
                                navController.navigate("workout_details/$workoutId")
                            } else {
                                notifyWorkoutNotSaved()
                            }
                        }
                    }
                )
            }
        }
    }

    private fun notifyWorkoutNotSaved() {
        Toast.makeText(applicationContext,getString(R.string.toast__workout_ignored), Toast.LENGTH_LONG).show();
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
                stopWorkoutService()
            } else {
                startWorkoutService()
            }
        }
    }

    private fun saveUserId(userId: Long) {
        val sharedPreferences: SharedPreferences =
            applicationContext.getSharedPreferences("shared", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(USER_ID_SHARED_PREFERENCES_KEY, userId)
        editor.apply()
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
        val intentStop = Intent(this, WorkoutService::class.java)
        stopService(intentStop)

    }

    private fun startWorkoutService() {
        val workoutServiceIntent = Intent(this, WorkoutService::class.java)
        ContextCompat.startForegroundService(this, workoutServiceIntent)
    }


    private fun checkPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }
}