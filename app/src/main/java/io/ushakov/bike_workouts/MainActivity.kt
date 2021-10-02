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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.view_models.WorkoutListViewModel
import io.ushakov.bike_workouts.view_models.WorkoutListViewModelFactory
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.ui.views.BluetoothSettings
import io.ushakov.bike_workouts.ui.views.Main
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme

/*
TODO Setup activity calls DB and gets user and it then pass UserId here, which should be store in shared preferences
*/
class MainActivity : ComponentActivity() {

    private lateinit var workoutListViewModel: WorkoutListViewModel
    private lateinit var workoutList: List<WorkoutSummary>

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HeartRateDeviceManager.initialize(applicationContext)

        workoutListViewModel = WorkoutListViewModelFactory(
            (application as WorkoutApplication).workoutRepository
        ).create(WorkoutListViewModel::class.java)
        //TODO get user id from preferences
        workoutListViewModel.getWorkoutsByUserId(1)

        setContent {
            val workoutList by workoutListViewModel.workoutsByUserId
            this.workoutList = workoutList
            BikeWorkoutsTheme {
                View()
            }
        }
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
                Main(navController, workoutList) {
                    if (ServiceStatus.IS_WORKOUT_SERVICE_RUNNING) {
                        stopWorkoutService()
                    } else {
                        startWorkoutService()
                    }
                }
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