package io.ushakov.bike_workouts

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.data_engine.WorkoutDataReceiver
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.Workout
import io.ushakov.bike_workouts.ui.theme.BikeWorkoutsTheme
import io.ushakov.bike_workouts.ui.views.*
import io.ushakov.bike_workouts.util.Constants
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.bike_workouts.util.rememberActiveWorkout
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

/*
TODO Setup activity calls DB and gets user and it then pass UserId here, which should be store in shared preferences
*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HeartRateDeviceManager.initialize(applicationContext)

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
        val application = rememberApplication()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        requestPermissions(bluetoothAdapter = bluetoothManager.adapter)

        rememberNavigateToUnfinishedWorkout(navController)
        rememberStartWorkoutService()

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

                //TODO Dummy Heart rate reading
                // Remove it later.
                Log.d("DBG", "Starting Dummy Heart rate reading....")

                /*CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        delay(1234
                        val intentForDataReceiver = Intent(ACTION_BROADCAST)
                        intentForDataReceiver.putExtra(EXTRA_HEART_RATE, Random.nextInt(50..150))
                        LocalBroadcastManager.getInstance(applicationContext)
                            .sendBroadcast(intentForDataReceiver)
                    }
                }*/

                return@DisposableEffect onDispose { }
            }

            val disposable = HeartRateDeviceManager.getInstance().subscribe {
                //TODO Forward this value to BroadCast receiver
                // Notify anyone listening for broadcasts about the Heart Rate

                /*val intentForDataReceiver = Intent(ACTION_BROADCAST)
                intentForDataReceiver.putExtra(EXTRA_HEART_RATE, it)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentForDataReceiver)*/

                Log.d("HEARTRATE", it.toString())
            }
            onDispose {
                disposable.dispose()
            }
        }

        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                Main(navController, 1) { startWorkout() }
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
                WorkoutDetails(navController,
                    workoutId = backStackEntry.arguments?.getLong("workoutId"))
            }
            composable("in_workout/{workoutId}",
                arguments = listOf(navArgument("workoutId") {
                    type = NavType.LongType
                })) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId")
                val workoutComplete by application.workoutRepository.getCompleteWorkoutById(
                    workoutId ?: return@composable)
                    .observeAsState()

                InWorkout(workoutComplete ?: return@composable) {
                    stopWorkout(workoutComplete?.workout ?: return@InWorkout)
                }
            }
        }
    }

    private fun stopWorkout(workout: Workout) {
        val application = application as WorkoutApplication
        val timeDifference = workout.startAt.time - Date().time

        lifecycleScope.launch {
            if (timeDifference > Constants.MINIMUM_WORKOUT_DURATION_MS) {
                application.workoutRepository.finishWorkout(workout.id)
                application.summaryRepository.insert(Summary(
                    workoutId = workout.id,
                    kiloCalories = 400,
                    distance = 200.0
                ))
            } else {
                application.workoutRepository.delete(workout)
            }
        }
    }

    private fun startWorkout() {
        lifecycleScope.launch {
            (application as WorkoutApplication).workoutRepository.startWorkout(1)
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun rememberNavigateToUnfinishedWorkout(navController: NavController) {
        val activeWorkout = rememberActiveWorkout()

        LaunchedEffect(key1 = activeWorkout) {
            if (activeWorkout == null) {
                if (navController.currentDestination?.route == "in_workout/{workoutId}") {
                    navController.popBackStack()
                    navController.navigate("main")
                }
            } else {
                navController.popBackStack()
                navController.navigate("in_workout/${activeWorkout.id}")
            }
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    private fun rememberStartWorkoutService() {
        val activeWorkout = rememberActiveWorkout()

        LaunchedEffect(key1 = activeWorkout) {
            if (activeWorkout == null) {
                stopWorkoutService()
            } else {
                startWorkoutService()
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