package io.ushakov.bike_workouts.data_engine

import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.Workout
import java.util.*

const val BodyWeightMultiplier = 3.5
const val PerMinuteDivider = 200

class WorkoutCaloriesProcessor {
    fun getCalories(workoutUser: User, workout: Workout, workoutDurationInMicroSeconds: Long): Int {

        //Calories burned per minute = (MET x body weight in Kg x 3.5) ÷ 200
        val userWeight = workoutUser.weight
        //Todo make MET enum class base on Activity type
        val met = workout.type
        val workoutDurationInMinutes = workoutDurationInMicroSeconds / 60000

        val caloriesPerMinute = (met * userWeight * BodyWeightMultiplier) / PerMinuteDivider

        return (caloriesPerMinute * workoutDurationInMinutes).toInt()
    }
}
