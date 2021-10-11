package io.ushakov.bike_workouts.ui.components

import android.location.Location.distanceBetween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.android.libraries.maps.model.LatLngBounds
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.db.entity.Location


@Composable
fun WorkoutMap(
    modifier: Modifier = Modifier,
    locations: List<Location>? = null,
    userLocation: LatLng? = null,
    focusOnUser: Boolean = false,
) {
    var map by remember { mutableStateOf<GoogleMap?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(map, userLocation) {
        if (map == null || userLocation == null) return@LaunchedEffect

        if (userMarker != null) {
            userMarker!!.position = userLocation
        } else {
            userMarker = map!!.addMarker(createUserMarker(userLocation))

            if (focusOnUser) {
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    userLocation,
                    11f
                ))
            }
        }
    }

    LaunchedEffect(locations, map) {
        val googleMap = map

        if (googleMap == null || locations == null || locations.isEmpty()) return@LaunchedEffect

        if (polyline == null) {
            polyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(locations.map { it.latLng })
                    .width(10f)
                    .color(android.graphics.Color.RED)
                    .endCap(RoundCap())
                    .startCap(RoundCap())
            )
        } else {
            polyline!!.points = locations.map { it.latLng }
        }


        val boundsBuilder = LatLngBounds.builder()
        locations.forEach { boundsBuilder.include(it.latLng) }

        val bounds = boundsBuilder.build()

        if (areBoundsTooSmall(bounds, 100)) {
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

fun createUserMarker(position: LatLng): MarkerOptions? {
    val blueDot =
        BitmapDescriptorFactory.fromResource(R.drawable.my_location)

    return MarkerOptions()
        .position(position)
        .icon(blueDot)
        .anchor(0.5f, 0.5f)
}