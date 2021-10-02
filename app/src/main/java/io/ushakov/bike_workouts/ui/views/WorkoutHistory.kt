package io.ushakov.bike_workouts.ui.views

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.ushakov.bike_workouts.R
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.ui.components.WorkoutList
import io.ushakov.myapplication.ui.theme.BikeWorkoutsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

@Composable
fun WorkoutHistory(
    navController: NavController,
    userId: Long,
) {
    val (workoutList, setWorkoutList) = remember { mutableStateOf<List<WorkoutSummary>?>(null) }

    val application = LocalContext.current.applicationContext as WorkoutApplication

    LaunchedEffect(userId) {
        val workoutsByUserId = async(Dispatchers.IO) {
            application.workoutRepository.getWorkoutsByUserId(userId)
        }

        setWorkoutList(workoutsByUserId.await())
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            topBar = { AppBar(navController = navController) },
        ) {
            if (workoutList != null) {
                WorkoutList(
                    workoutSummaryList = workoutList,
                    onSelected = { workoutSummary ->
                        Log.d("DBG", "Workout Id: ${workoutSummary.workout!!.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun AppBar(navController: NavController) {
    TopAppBar(
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