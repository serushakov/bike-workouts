package io.ushakov.bike_workouts.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val firstName: String,
    val lastName: String,
    val height: Int,
    val weight: Int
    ){

    constructor(
        @NotNull firstName: String,
        @NotNull lastName: String
    ) : this(
        0,firstName, lastName, 0, 0
    )

    constructor(
        @NotNull firstName: String,
        @NotNull lastName: String,
        @NotNull height: Int,
        @NotNull weight: Int
    ) : this(
        0,firstName, lastName, height, weight
    )
}

class UserWorkout {

    @Embedded
    var user: User? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    var workouts: List<Workout>? = null
}
//TODO Remove this class later. Replaced by WorkoutSummary
class UserWorkoutListItem {

    var userId: Long? = null
    var workoutId: Long? = null
    var startAt: Date? = null
    var distance: Double? = null
    var kiloCalories: Int? = null
}