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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.data_engine.DataReceiver
import io.ushakov.bike_workouts.ui.views.BluetoothSettings
import io.ushakov.bike_workouts.ui.views.Main
import io.ushakov.bike_workouts.ui.views.WorkoutHistory
import io.ushakov.bike_workouts.util.Constants.ACTION_BROADCAST
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import kotlinx.coroutines.*

/*
TODO Setup activity calls DB and gets user and it then pass UserId here, which should be store in shared preferences
*/
class MainActivity : ComponentActivity() {


    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HeartRateDeviceManager.initialize(applicationContext)
        //TODO Start workout service. (Temp code, remove later)
        // Added for testing, later we decide from where it will gonna start.
        startWorkoutService()

        //Initialize Broadcast receiver
        val dataReceiver = DataReceiver()
        dataReceiver.let {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(it,IntentFilter(ACTION_BROADCAST))
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


    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
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

                //TODO Dummy Heart rate reading
                // Remove it later.
                Log.d("DBG", "Starting Dummy Heart rate reading....")

                /*CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        delay(1234)
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