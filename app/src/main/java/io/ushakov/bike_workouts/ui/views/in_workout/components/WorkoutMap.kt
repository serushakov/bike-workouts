package io.ushakov.bike_workouts.ui.views.in_workout.components

import android.location.Location.distanceBetween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.ui.components.ComposableMap
import com.google.android.libraries.maps.model.LatLngBounds


@Composable
fun WorkoutMap(
    locations: List<Location>,
    modifier: Modifier = Modifier,
) {
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    LaunchedEffect(locations) {
        val googleMap = map ?: return@LaunchedEffect

        if (polyline == null) {
            polyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(locations.map { LatLng(it.latitude, it.longitude) })
                    .width(10f)
                    .color(android.graphics.Color.RED)
                    .endCap(RoundCap())
                    .startCap(RoundCap())
            )
        } else {
            polyline!!.points = locations.map { LatLng(it.latitude, it.longitude) }
        }


        val boundsBuilder = LatLngBounds.builder()
        locations.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }

        val bounds = boundsBuilder.build()

        if(areBoundsTooSmall(bounds, 100)) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 16f))
        } else {

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }


    }

    ComposableMap(onUpdate = { googleMap ->
        map = googleMap
    }, modifier = modifier)
}

fun areBoundsTooSmall(bounds: LatLngBounds, minDistanceInMeter: Int): Boolean {
    val result = FloatArray(1)

    distanceBetween(bounds.southwest.latitude,
        bounds.southwest.longitude,
        bounds.northeast.latitude,
        bounds.northeast.longitude,
        result)
    return result[0] < minDistanceInMeter
}