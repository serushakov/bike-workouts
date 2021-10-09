package io.ushakov.bike_workouts.ui.views.in_workout.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import io.ushakov.bike_workouts.db.entity.Location
import io.ushakov.bike_workouts.ui.components.ComposableMap

@Composable
fun WorkoutMap(
    locations: List<Location>,
    modifier: Modifier = Modifier
) {
    var map by remember { mutableStateOf<GoogleMap?>(null)}
    var polyline by remember { mutableStateOf<Polyline?>(null)}

    LaunchedEffect(locations) {
        val googleMap = map ?: return@LaunchedEffect

        if(polyline == null) {
            polyline = googleMap.addPolyline(
                PolylineOptions()
                    .addAll(locations.map { LatLng(it.latitude, it.longitude) })
                    .width(2.5f)
                    .color(android.graphics.Color.RED)
                    .endCap(RoundCap())
                    .startCap(RoundCap())
            )
        } else {
            polyline!!.points = locations.map { LatLng(it.latitude, it.longitude) }
        }

        val bounds = LatLngBounds.builder()
        locations.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }

        googleMap.setLatLngBoundsForCameraTarget(bounds.build())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 30))
    }

    ComposableMap(onUpdate = { googleMap ->
        map = googleMap
    }, modifier = modifier)
}