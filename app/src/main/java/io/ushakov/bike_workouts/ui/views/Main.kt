package io.ushakov.bike_workouts.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.MapView
import com.google.maps.android.ktx.awaitMap
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.components.SectionTitle
import io.ushakov.bike_workouts.ui.components.WorkoutColumnItem
import kotlinx.coroutines.launch
import java.util.*
import com.google.android.libraries.maps.CameraUpdateFactory

import com.google.android.libraries.maps.model.LatLng

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.ushakov.bike_workouts.MainActivity
import com.google.android.libraries.maps.model.MarkerOptions


import com.google.android.libraries.maps.model.BitmapDescriptorFactory

import com.google.android.libraries.maps.model.BitmapDescriptor
import io.ushakov.myapplication.ui.theme.Typography


@Composable
fun Main(navController: NavController) {
    Scaffold(
        topBar = { MainAppBar(navController) }
    ) {
        MainMapView()
    }
}

@Composable
fun MainMapView() {
    val scope = rememberCoroutineScope()
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current);

    if (ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }


    Box {
        AndroidView(
            modifier = Modifier.padding(top = 40.dp),
            factory = { mapView }
        ) { mapView ->
            scope.launch {
                val map = mapView.awaitMap()
                val locationResult = fusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(
                    context as MainActivity
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        if (task.result != null) {
                            val location = LatLng(task.result.latitude, task.result.longitude)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                location,
                                11f
                            ))
                            val marker = UserMarker(location)
                            map.addMarker(marker)
                        }
                    }
                }
            }
        }

        Column(Modifier
            .height(400.dp)
            .fillMaxWidth()
            .background(brush = Brush.verticalGradient(colors = listOf(Color.White,
                Color.Transparent), startY = 250f))
            .align(Alignment.TopCenter)
            .padding(horizontal = 16.dp)
        ) {
            SectionTitle(text = "Last workout")
            WorkoutColumnItem(date = Date(), distance = 25.0, kcal = 400) {

            }
        }


        Column(
            Modifier
                .height(400.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent,
                    Color.White)))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            ExtendedFloatingActionButton(
                text = {
                    Text(text = "START")
                },
                onClick = {}
            )
        }
    }
}

fun UserMarker(position: LatLng): MarkerOptions? {
    val blueDot =
        BitmapDescriptorFactory.fromResource(R.drawable.my_location)

    return MarkerOptions()
        .position(position)
        .title("Your location")
        .icon(blueDot)
        .anchor(0.5f, 0.5f)
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    // Makes MapView follow the lifecycle of this composable
    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
        }
    }

@Composable
fun MainAppBar(navController: NavController) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.AccountCircle, "Account")
            }
        },
        title = {},
        actions = {
            IconButton(onClick = { navController.navigate("workout_history") }) {
                Icon(Icons.Default.History, "Workout history")
            }
            IconButton(onClick = { navController.navigate("bluetooth_settings") }) {
                Icon(Icons.Default.Bluetooth, "Bluetooth")
            }
        },
        backgroundColor = Color.White,
        elevation = 0.dp
    )
}