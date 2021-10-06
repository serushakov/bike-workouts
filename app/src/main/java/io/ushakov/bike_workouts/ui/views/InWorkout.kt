package io.ushakov.bike_workouts.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.ushakov.bike_workouts.WorkoutApplication
import io.ushakov.bike_workouts.db.entity.Summary
import io.ushakov.bike_workouts.db.entity.WorkoutComplete
import io.ushakov.bike_workouts.util.Constants
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun InWorkout(workoutComplete: WorkoutComplete?) {
    val (
        workout,
        heartRates,
        locations
    ) = workoutComplete ?: return

    if(workout == null || heartRates == null || locations == null) return

    val application = LocalContext.current.applicationContext as WorkoutApplication
    val scope = rememberCoroutineScope()

    Surface {
        Column(Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                val timeDifference = workout.startAt.time - Date().time

                scope.launch {
                    if(timeDifference > Constants.MINIMUM_WORKOUT_DURATION_MS) {
                        application.workoutRepository.finishWorkout(workout.id)
                        application.summaryRepository.insert(Summary(
                            workoutId = workout.id,
                            kiloCalories = 400,
                            distance = 200.0
                        ))
                    } else {
                        application.workoutRepository.delete(workout)
                    }
                }
            }) {
                Text("STOP")
            }
        }
    }
}