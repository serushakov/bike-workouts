package io.ushakov.bike_workouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import kotlinx.coroutines.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BikeWorkoutsTheme {
                View()
            }
        }
    }


    @Composable
    fun View() {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val scaffoldState = ScaffoldState(
                DrawerState(DrawerValue.Closed),
                snackbarHostState = SnackbarHostState()
            )

            Scaffold(
                bottomBar = { AppBar(scaffoldState) },
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
    fun AppBar(scaffoldState: ScaffoldState) {
        val scope = rememberCoroutineScope()

        BottomAppBar(
            cutoutShape = RoundedCornerShape(50)
        ) {
            UserIconButton(scaffoldState = scaffoldState, scope = scope)
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

    @Preview(widthDp = 480, heightDp = 840)
    @Composable
    fun Preview() {
        BikeWorkoutsTheme {
            View()
        }
    }
}