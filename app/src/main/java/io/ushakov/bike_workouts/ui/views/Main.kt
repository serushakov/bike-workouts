package io.ushakov.bike_workouts.ui.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.maps.model.LatLng
import io.ushakov.bike_workouts.MainActivity
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.UserWorkouts
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.ui.components.*
import io.ushakov.bike_workouts.ui.theme.Blue800
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlay
import io.ushakov.bike_workouts.ui.theme.PrimaryOverlayDark
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.rememberApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Main(
    navController: NavController,
    userId: Long,
    deviceConnected: Boolean,
    onStartButtonClick: () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(drawerContent = { Drawer(userId) },
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState) {
        Scaffold(
            topBar = {
                MainAppBar(navController, deviceConnected) {
                    scope.launch {
                        drawerState.open()
                    }
                }
            }
        ) {
            Box(Modifier.fillMaxWidth()) {
                WorkoutMap(locations = null,
                    userLocation = rememberUserLocation(),
                    focusOnUser = true,
                    modifier = Modifier.padding(top = 40.dp))

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
                        onClick = onStartButtonClick
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppBar(
    navController: NavController,
    deviceConnected: Boolean,
    onNavigationIconClick: () -> Unit,
) {
    ThemedTopAppBar(
        navigationIcon = {
            IconButton(onClick = { onNavigationIconClick() }) {
                Icon(Icons.Default.AccountCircle, stringResource(R.string.main__account_icon_label))
            }
        },
        title = {},
        actions = {
            IconButton(onClick = { navController.navigate("workout_history") }) {
                Icon(Icons.Default.History, stringResource(R.string.main__history_icon_label))
            }

            IconButton(onClick = { navController.navigate("bluetooth_settings") }) {
                Icon(Icons.Default.Bluetooth,
                    stringResource(R.string.main__bluetooth_icon_label),
                    tint = if (deviceConnected) Color.Green else Color.Unspecified)
            }
        },
        elevation = 0.dp
    )
}

@Composable
fun LastWorkoutItem(navController: NavController, userId: Long) {
    val application = LocalContext.current.applicationContext as WorkoutApplication

    var lastWorkout by remember { mutableStateOf<WorkoutSummary?>(null) }
    var noLastWorkout by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        lastWorkout = withContext(Dispatchers.IO) {
            val workout = application.workoutRepository.getLastWorkout(userId)

            if (workout == null) {
                noLastWorkout = true
            }

            workout
        }
    }

    val workout = lastWorkout?.workout
    val summary = lastWorkout?.summary

    if (noLastWorkout) {
        FirstWorkoutBanner()
        return
    }

    if (workout == null || summary == null) return

    SectionTitle(text = stringResource(R.string.main__last_workout))
    WorkoutColumnItem(date = workout.startAt,
        distance = summary.distance,
        kcal = summary.kiloCalories) {
        navController.navigate("workout_details/${workout.id}")
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
        return null
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

@Composable
fun Drawer(userId: Long) {
    val darkTheme = isSystemInDarkTheme()
    val application = rememberApplication()
    var userWorkouts by remember(userId) { mutableStateOf<UserWorkouts?>(null) }

    LaunchedEffect(userId) {
        userWorkouts = application.userRepository.getUserWithWorkouts(userId)
    }

    if (userWorkouts?.user == null) return

    Column(Modifier.fillMaxWidth()) {
        Column() {
            Image(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                painter = painterResource(R.drawable.drawer_background),
                contentScale = ContentScale.FillWidth,

                contentDescription = stringResource(
                    R.string.drawer__background)
            )

            Surface {
                Column(
                    Modifier.padding(all = 16.dp)
                ) {
                    Row() {
                        Box(
                            content = {
                                Icon(Icons.Default.AccountCircle,
                                    stringResource(R.string.drawer__account),
                                    Modifier.size(32.dp),
                                    tint = if (darkTheme) MaterialTheme.colors.onSurface else Blue800)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (darkTheme) PrimaryOverlayDark else PrimaryOverlay)
                                .align(CenterVertically),
                            contentAlignment = Alignment.Center
                        )

                        Column(Modifier.padding(start = 16.dp),
                            verticalArrangement = Arrangement.Center) {
                            Text(text = userWorkouts!!.user!!.firstName,
                                style = Typography.h6)
                            Spacer(Modifier.height(4.dp))
                            Text(text = "Workouts: ${userWorkouts?.workouts?.size ?: 0}", style = Typography.body2)
                        }
                    }
                }
            }

        }


    }
}

