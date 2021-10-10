package io.ushakov.bike_workouts.ui.views.first_time_setup

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.ui.theme.Typography

@Composable
fun FirstTimeSetup() {
    val navController = rememberNavController()
    var nameInputValue by remember { mutableStateOf<String?>(null) }


    NavHost(navController = navController, startDestination = "name") {
        composable("name") {
            Name(initialName = nameInputValue) {
                nameInputValue = it
                navController.navigate("measurements")
            }
        }
        composable("measurements") {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Column {

                    Text(text = "Measurements")
                    Button(onClick = {
                        navController.navigate("permissions")
                    }) {
                        Text("Next")
                    }
                }

            }
        }
        composable("permissions") {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Permissions(onPermissionGranted = {
                    navController.navigate("finish")
                })
            }
        }
        composable("finish") {
            BackHandler {
                navController.popBackStack("measurements", false)
            }

            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = "Finish")
            }
        }
    }
}


@Composable
fun Name(initialName: String?, onNameSubmit: (name: String) -> Unit) {
    var nameInputValue by remember { mutableStateOf(initialName ?: "") }
    val insets = LocalWindowInsets.current

    val imeBottom = with(LocalDensity.current) { insets.ime.bottom.toDp() }

    Log.d("insets", imeBottom.toString())

    Surface {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier
                .weight(1f)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.intro__name__titile),
                    style = Typography.h2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(Modifier
                .weight(1f)
                .padding(horizontal = 30.dp)
                .padding(bottom = 30.dp)
            ) {
                Text(
                    text = stringResource(R.string.intro__name__subtitle),
                    style = Typography.h5
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = nameInputValue,
                    onValueChange = { nameInputValue = it },
                    label = { Text(stringResource(R.string.intro__name__input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrect = false,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNameSubmit(nameInputValue) }
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onNameSubmit(nameInputValue) },
                    enabled = nameInputValue.trim() !== "",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(R.string.intro__continue_button)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Permissions(onPermissionGranted: () -> Unit) {
    val locationPermissionState = rememberPermissionState(ACCESS_FINE_LOCATION)


    LaunchedEffect(locationPermissionState.hasPermission,
        locationPermissionState.shouldShowRationale,
        locationPermissionState.permissionRequested) {

        Log.d("permisisons",
            "hasPermission: ${locationPermissionState.hasPermission}, permissionRequested: ${locationPermissionState.permissionRequested}, shouldShowRationale: ${locationPermissionState.shouldShowRationale}")

        if (locationPermissionState.hasPermission) {
            onPermissionGranted()
        }
    }


    PermissionRequired(
        permissionState = locationPermissionState,
        permissionNotGrantedContent = {
            Text("gib permison")
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Gib")
            }
        },
        permissionNotAvailableContent = {
            Surface(
                modifier = Modifier.fillMaxHeight()
            ) {
                Text("y u no gib")
            }
        }
    ) {}
}
