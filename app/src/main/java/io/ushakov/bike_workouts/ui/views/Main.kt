package io.ushakov.bike_workouts.ui.views

import android.os.Bundle
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.google.android.libraries.maps.MapView
import com.google.maps.android.ktx.awaitMap
import io.ushakov.bike_workouts.R
import kotlinx.coroutines.launch


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

    AndroidView(
        { mapView }
    ) { mapView ->
        scope.launch {
            val map = mapView.awaitMap()
            map.uiSettings.isZoomControlsEnabled = true
        }
    }
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