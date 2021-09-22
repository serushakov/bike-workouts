package io.ushakov.bike_workouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        TopAppBar(
            title = { Text("Hello world") }
        )
    }


    @Composable
    fun View() {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Scaffold(
                topBar = { AppBar() },
                floatingActionButton = {
                    FloatingActionButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Add, "Start workout")
                    }
                },
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