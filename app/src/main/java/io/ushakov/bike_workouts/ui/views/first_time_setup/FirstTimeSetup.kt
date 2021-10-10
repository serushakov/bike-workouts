package io.ushakov.bike_workouts.ui.views.first_time_setup

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import io.ushakov.bike_workouts.ui.views.first_time_setup.components.Measurements

@Composable
fun FirstTimeSetup() {
    val navController = rememberNavController()
    var name by remember { mutableStateOf<String?>(null) }
    var age by remember { mutableStateOf<Int?>(null) }
    var weight by remember { mutableStateOf<Int?>(null) }


    NavHost(navController = navController, startDestination = "name") {
        composable("name") {
            Name(initialName = name) {
                name = it
                navController.navigate("measurements")
            }
        }
        composable("measurements") {
            Measurements(
                name ?: return@composable,
                age,
                weight
            ) { resultAge, resultWeight ->
                age = resultAge
                weight = resultWeight
                navController.navigate("permissions")
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
