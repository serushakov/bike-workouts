package io.ushakov.bike_workouts.ui.views.first_time_setup.components

import android.Manifest
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import io.ushakov.bike_workouts.ui.theme.Typography

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.ushakov.bike_workouts.util.Constants


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(onPermissionGranted: () -> Unit) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current

    Surface {
        PermissionRequired(
            permissionState = locationPermissionState,
            permissionNotGrantedContent = {
                Layout(
                    titleText = "Permissions"
                ) {
                    Text(
                        text = "Almost done! \n App requires Location permissions to save your rides and calculate calories and distances. Also, it enables Bluetooth!",
                        style = Typography.body1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.fillMaxHeight(0.5f))
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Continue"
                        )
                    }
                }
            },
            permissionNotAvailableContent = {
                val lifecycle = LocalLifecycleOwner.current.lifecycle

                // When user returns to the app after changing permissions
                // in the settings app we need to manually re-check permissions
                // Unfortunately, locationPermissionState does not update
                DisposableEffect(null) {
                    val lifecycleObserver = LifecycleEventObserver { _, event ->
                        // When app returns from
                        if (
                            event == Lifecycle.Event.ON_RESUME &&
                            !locationPermissionState.hasPermission &&
                            ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        ) {
                            onPermissionGranted()
                        }
                    }

                    lifecycle.addObserver(lifecycleObserver)
                    onDispose {
                        lifecycle.removeObserver(lifecycleObserver)
                    }
                }

                Layout(
                    titleText = "No permissions 😕"
                ) {
                    Text(
                        text = "App requires access to location to function properly. Please go to Settings and allow BikeWorkouts to use location.",
                        style = Typography.body1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.fillMaxHeight(0.5f))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val uri: Uri =
                                Uri.fromParts("package", Constants.PACKAGE_NAME, null)
                            intent.data = uri
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Take me there".uppercase()
                        )
                    }
                }
            }
        ) {
            LaunchedEffect(null) {
                onPermissionGranted()
            }
        }
    }
}