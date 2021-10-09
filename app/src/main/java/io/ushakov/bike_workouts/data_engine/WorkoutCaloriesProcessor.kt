package io.ushakov.bike_workouts.data_engine

import io.ushakov.bike_workouts.db.entity.User
import io.ushakov.bike_workouts.db.entity.Workout

class WorkoutCaloriesProcessor {
    fun getCalories(workoutUser: User, workout: Workout): Int {
        var kCal = 0
        if (workoutUser != null && workout != null) {
            //Calories burned per minute = (MET x body weight in Kg x 3.5) ÷ 200
            val userWeight = workoutUser.weight
            //Todo make MET enum class base on Activity type
            val met = workout.type
            val workoutDurationInMinutes = workout.finishAt?.time?.minus(workout.startAt.time)?.div(60)
            if (workoutDurationInMinutes != null) {
                kCal = (met.times(userWeight).times(3.5)).div(200).times(workoutDurationInMinutes).toInt()
            }
        }
        return kCal
    }

}
