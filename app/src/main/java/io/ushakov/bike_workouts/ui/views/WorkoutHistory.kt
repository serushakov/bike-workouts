package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.ui.components.ThemedTopAppBar
import io.ushakov.bike_workouts.ui.components.WorkoutList
import io.ushakov.bike_workouts.ui.theme.BikeWorkoutsTheme
import kotlinx.coroutines.launch

@Composable
fun WorkoutHistory(
    navController: NavController,
    userId: Long,
) {
    val application = LocalContext.current.applicationContext as WorkoutApplication
    val workoutList by application.workoutRepository.getWorkoutsByUserId(userId).observeAsState()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            topBar = { AppBar(navController = navController) },
        ) {
            if (workoutList != null) {
                WorkoutList(
                    workoutSummaryList = workoutList!!,
                    onSelect = { workoutSummary ->
                        navController.navigate("workout_details/${workoutSummary.workout!!.id}")
                    },
                    onDelete = { workoutId ->
                        scope.launch {
                            application.workoutRepository.deleteById(workoutId = workoutId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppBar(navController: NavController) {
    ThemedTopAppBar(
        title = { Text("Workout history") },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.navigation_back))
            }
        })
}

@Preview(widthDp = 480, heightDp = 840)
@Composable
internal fun MainPreview() {
    BikeWorkoutsTheme {
        WorkoutHistory(rememberNavController(), 1)
    }
}