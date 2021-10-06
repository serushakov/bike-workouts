package io.ushakov.bike_workouts.db.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            onDelete = CASCADE,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    val userId: Long,
    val title: String,
    val type: String,
    val startAt: Date,
    var finishAt: Date?,
) {

    constructor(
        @NotNull userId: Long,
        @NotNull title: String,
        @NotNull type: String,
        @NotNull startAt: Date,
        finishAt: Date? = null,
    ) : this(
        0, userId, title, type, startAt, finishAt
    )
}

class WorkoutHeartRate {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var heartRates: List<HeartRate>? = null
}

class WorkoutLocation {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var locations: List<Location>? = null
}

class WorkoutSummary {
    @Embedded
    var workout: Workout? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var summary: Summary? = null
}

data class WorkoutComplete(
    @Embedded
    var workout: Workout? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var heartRates: List<HeartRate>? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var locations: List<Location>? = null,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var summary: Summary? = null,
)