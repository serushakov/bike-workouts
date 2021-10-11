package io.ushakov.bike_workouts.ui.views.first_time_setup.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Typography
import io.ushakov.bike_workouts.util.Constants

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(reRequestingPermissions: Boolean = false, onPermissionGranted: () -> Unit) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current

    Surface {
        PermissionRequired(
            permissionState = locationPermissionState,
            permissionNotGrantedContent = {
                Layout(
                    titleText = stringResource(R.string.intro__permissions__1_title)
                ) {
                    Text(
                        text = if (reRequestingPermissions) stringResource(R.string.intro__permissions__1_text_re_request) else stringResource(
                            R.string.intro__permissions__1_text),
                        style = Typography.body1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.fillMaxHeight(0.5f))
                    Button(
                        onClick = { locationPermissionState.launchPermissionRequest() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = stringResource(R.string.intro__permissions__1__button).uppercase())
                    }
                }
            },
            permissionNotAvailableContent = {
                val onSettingsButtonClick =
                    checkPermissionsOnResume(locationPermissionState = locationPermissionState) {
                        onPermissionGranted()
                    }

                Layout(
                    titleText = stringResource(R.string.intro__permissions__2__title)
                ) {
                    Text(
                        text = stringResource(R.string.intro__permissions__2__text),
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
                            onSettingsButtonClick()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = stringResource(R.string.intro__permissions__settings_button).uppercase()
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


// When user returns to the app after changing permissions
// in the settings app we need to manually re-check permissions
// Unfortunately, locationPermissionState does not update
@ExperimentalPermissionsApi
@Composable
fun checkPermissionsOnResume(
    locationPermissionState: PermissionState,
    onPermissionGranted: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var permissionRequested by remember { mutableStateOf(false) }

    DisposableEffect(null) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            val permissionGranted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (
                        !locationPermissionState.hasPermission &&
                        permissionGranted
                    ) {
                        onPermissionGranted()
                    } else if (event == Lifecycle.Event.ON_RESUME && !permissionGranted && !permissionRequested) {
                        locationPermissionState.launchPermissionRequest()
                        permissionRequested = true
                    }
                }
                else -> {}
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return {
        permissionRequested = false
    }
}