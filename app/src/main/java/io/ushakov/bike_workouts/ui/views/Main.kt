package io.ushakov.bike_workouts.ui.views


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
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
import com.google.android.gms.location.*
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.maps.android.ktx.awaitMap
import io.ushakov.bike_workouts.MainActivity
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.components.SectionTitle
import io.ushakov.bike_workouts.ui.components.WorkoutColumnItem
import kotlinx.coroutines.launch
import java.util.*


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


    Box {
        MapView()

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

@Composable
fun MapView() {
    val scope = rememberCoroutineScope()
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }

    if (ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    DisposableEffect(fusedLocationProviderClient) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                val last = result?.locations?.last()

                userLocation =
                    if (last != null) LatLng(last.latitude, last.longitude) else userLocation
            }
        }

        val locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000

        // RequestLocationUpdates
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

        val locationResult = fusedLocationProviderClient.lastLocation

        // Set map to last known location
        locationResult.addOnCompleteListener(
            context as MainActivity
        ) { task ->
            if (task.isSuccessful) {
                if (task.result != null) {
                    val location = LatLng(task.result.latitude, task.result.longitude)

                    userLocation = location
                }
            }
        }


        onDispose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(map, userLocation) {
        if (map == null || userLocation == null) return@LaunchedEffect

        if (userMarker != null) {
            userMarker!!.position = userLocation
        } else {
            userMarker = map!!.addMarker(createUserMarker(userLocation!!))
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                userLocation,
                11f
            ))
        }

    }

    AndroidView(
        modifier = Modifier.padding(top = 40.dp),
        factory = { mapView }
    ) { mapView ->
        scope.launch {
            map = mapView.awaitMap()
        }
    }
}

fun createUserMarker(position: LatLng): MarkerOptions? {
    val blueDot =
        BitmapDescriptorFactory.fromResource(R.drawable.my_location)

    return MarkerOptions()
        .position(position)
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