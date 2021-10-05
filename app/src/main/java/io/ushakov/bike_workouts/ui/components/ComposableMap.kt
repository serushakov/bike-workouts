package io.ushakov.bike_workouts.ui.components

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.GoogleMapOptions
import com.google.android.libraries.maps.MapView
import com.google.maps.android.ktx.awaitMap
import io.ushakov.bike_workouts.R
import kotlinx.coroutines.launch


@Composable
fun ComposableMap(modifier: Modifier = Modifier, onInit: (googleMap: GoogleMap) -> Unit) {
    val mapViewWithLifecycle = rememberMapViewWithLifecycle()
    val scope = rememberCoroutineScope()

    var mapReady by remember { mutableStateOf(false) }
    val alpha: Float by animateFloatAsState(
        if (mapReady) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )


    AndroidView(
        modifier = modifier.alpha(alpha),
        factory = { mapViewWithLifecycle }
    ) { mapView ->
        scope.launch {
            val map = mapView.awaitMap()
            onInit(map)

            map.setOnMapLoadedCallback { mapReady = true }
        }
    }
}


@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val darkMode = isSystemInDarkTheme()

    Log.d("dark mode", darkMode.toString())
    val mapView = remember(darkMode) {
        MapView(context,
            GoogleMapOptions().mapId(if (darkMode) context.getString(R.string.dark_mode_map_id) else context.getString(
                R.string.light_mode_map_id))).apply {
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