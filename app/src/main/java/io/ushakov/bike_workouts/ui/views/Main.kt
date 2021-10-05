package io.ushakov.bike_workouts.ui.views


import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.MainActivity
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.ui.components.ComposableMap
import io.ushakov.bike_workouts.ui.components.SectionTitle
import io.ushakov.bike_workouts.ui.components.ThemedTopAppBar
import io.ushakov.bike_workouts.ui.components.WorkoutColumnItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


@Composable
fun Main(navController: NavController, userId: Long) {
    Scaffold(
        topBar = { MainAppBar(navController) }
    ) {
        Box(Modifier.fillMaxWidth()) {
            MapView()

            Column(Modifier
                .height(400.dp)
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(MaterialTheme.colors.surface,
                    Color.Transparent), startY = 250f))
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
            ) {
                LastWorkoutItem(navController = navController, userId = userId)
            }

            Column(
                Modifier
                    .height(400.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent,
                        MaterialTheme.colors.surface)))
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
}

@Composable
fun MainAppBar(navController: NavController) {
    ThemedTopAppBar(
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
        elevation = 0.dp
    )
}

@Composable
fun LastWorkoutItem(navController: NavController, userId: Long) {
    val application = LocalContext.current.applicationContext as WorkoutApplication

    var lastWorkout by remember { mutableStateOf<WorkoutSummary?>(null) }

    LaunchedEffect(userId) {
        lastWorkout = withContext(Dispatchers.IO) {
            application.workoutRepository.getLastWorkout(userId)
        }
    }

    val workout = lastWorkout?.workout
    val summary = lastWorkout?.summary
    if (workout == null || summary == null) return

    SectionTitle(text = "Last workout")
    WorkoutColumnItem(date = workout.startAt,
        distance = summary.distance,
        kcal = summary.kiloCalories) {
        navController.navigate("workout_details/${workout.id}")
    }
}

@Composable
fun MapView() {
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    val userLocation = rememberUserLocation()


    LaunchedEffect(map, userLocation) {
        if (map == null || userLocation == null) return@LaunchedEffect

        if (userMarker != null) {
            userMarker!!.position = userLocation
        } else {
            userMarker = map!!.addMarker(createUserMarker(userLocation))
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                userLocation,
                11f
            ))
        }
    }

    ComposableMap(
        modifier = Modifier.padding(top = 40.dp),
    ) { googleMap ->
        map = googleMap
    }
}

@Composable
fun rememberUserLocation(): LatLng? {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }



    if (ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        return userLocation
    }

    DisposableEffect(fusedLocationProviderClient) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                val last = result?.locations?.last()

                userLocation =
                    if (last != null) LatLng(last.latitude, last.longitude) else userLocation
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000

        // Request location updates
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

    return userLocation
}

fun createUserMarker(position: LatLng): MarkerOptions? {
    val blueDot =
        BitmapDescriptorFactory.fromResource(R.drawable.my_location)

    return MarkerOptions()
        .position(position)
        .icon(blueDot)
        .anchor(0.5f, 0.5f)
}


