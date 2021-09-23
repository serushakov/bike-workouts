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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme


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
    fun AppBar() {
        BottomAppBar(
            cutoutShape = RoundedCornerShape(50)
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.AccountCircle, stringResource(R.string.account_button_label))
            }
        }
    }


    @Composable
    fun View() {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Scaffold(
                bottomBar = { AppBar() },
                floatingActionButton = {
                    FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Add, stringResource(R.string.start_workout_label))
                    }
                },
                isFloatingActionButtonDocked = true,
                floatingActionButtonPosition = FabPosition.Center,

                ) { }
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