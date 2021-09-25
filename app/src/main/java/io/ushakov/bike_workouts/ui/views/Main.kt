package io.ushakov.bike_workouts.ui.views

import io.ushakov.bike_workouts.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Main(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val scaffoldState = ScaffoldState(
            DrawerState(DrawerValue.Closed),
            snackbarHostState = SnackbarHostState()
        )

        Scaffold(
            bottomBar = { AppBar(scaffoldState, navController) },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                }) {
                    Icon(Icons.Filled.Add, stringResource(R.string.start_workout_label))
                }
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.Center,
            drawerContent = {
                Text("Hello")
            },
            scaffoldState = scaffoldState
        ) { }
    }
}

@Composable
fun AppBar(scaffoldState: ScaffoldState, navController: NavController) {
    val scope = rememberCoroutineScope()

    BottomAppBar(
        cutoutShape = RoundedCornerShape(50)
    ) {
        UserIconButton(scaffoldState = scaffoldState, scope = scope)
        Spacer(Modifier.weight(1f, true))
        SettingsButton(navController)
    }
}

@Composable
fun UserIconButton(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    IconButton(onClick = {
        scope.launch {
            scaffoldState.drawerState.open()
        }
    }) {
        Icon(Icons.Filled.AccountCircle, stringResource(R.string.account_button_label))
    }
}

@Composable
fun SettingsButton(navController: NavController) {
    IconButton(onClick = {
        navController.navigate("bluetooth_settings")
    }) {
        Icon(Icons.Default.Bluetooth, stringResource(R.string.settings_button_label))
    }
}

@Preview(widthDp = 480, heightDp = 840)
@Composable
internal fun MainPreview() {
    BikeWorkoutsTheme {
        Main(rememberNavController())
    }
}