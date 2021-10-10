package io.ushakov.bike_workouts.ui.views.first_time_setup

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Typography

@Composable
fun FirstTimeSetup() {
    val navController = rememberNavController()
    var nameInputValue by remember { mutableStateOf<String?>(null) }


    NavHost(navController = navController, startDestination = "name") {
        composable("name") {
            Name(initialName = name) {
                name = it
                navController.navigate("measurements")
            }
        }
        composable("measurements") {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Column {

                    Text(text = "Measurements")
                    Button(onClick = {
                        navController.navigate("permissions")
                    }) {
                        Text("Next")
                    }
                }

            }
        }
        composable("permissions") {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Permissions(onPermissionGranted = {
                    navController.navigate("finish")
                })
            }
        }
        composable("finish") {
            BackHandler {
                navController.popBackStack("measurements", false)
            }

            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = "Finish")
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(onPermissionGranted: () -> Unit) {
    val locationPermissionState = rememberPermissionState(ACCESS_FINE_LOCATION)


    LaunchedEffect(locationPermissionState.hasPermission,
        locationPermissionState.shouldShowRationale,
        locationPermissionState.permissionRequested) {

        Log.d("permisisons",
            "hasPermission: ${locationPermissionState.hasPermission}, permissionRequested: ${locationPermissionState.permissionRequested}, shouldShowRationale: ${locationPermissionState.shouldShowRationale}")

        if (locationPermissionState.hasPermission) {
            onPermissionGranted()
        }
    }


    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            Text("gib permison")
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Gib")
            }
        },
        permissionNotAvailableContent = {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text("y u no gib")
            }
        }
    ) {}
}
