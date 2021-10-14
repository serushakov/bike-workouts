package io.ushakov.bike_workouts.data_engine

import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.Workout

const val BodyWeightMultiplier = 3.5
const val PerMinuteDivider = 200

class WorkoutCaloriesProcessor {

    //Calories burned per minute = (MET x body weight in Kg x 3.5) ÷ 200
    fun getCalories(userWeight: Int, met: Int, cyclingDuration: Double): Int {
        val workoutDurationInMinutes = cyclingDuration / 60

        val caloriesPerMinute = (met * userWeight * BodyWeightMultiplier) / PerMinuteDivider

        return (caloriesPerMinute * workoutDurationInMinutes).toInt()
    }
}
