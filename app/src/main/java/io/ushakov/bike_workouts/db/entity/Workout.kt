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
    val id: Long,
    val userId: Long,
    val title: String,
    val type: String,
    val startAt: Date,
    val finishAt: Date
    ) {

    constructor(
        @NotNull userId: Long,
        @NotNull title: String,
        @NotNull type: String,
        @NotNull startAt: Date,
        @NotNull finishAt: Date
    ) : this(
        0, userId, title, type, startAt, finishAt
    )
}

class WorkoutHeartRate {

    @Embedded
    var heartRate: HeartRate? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var heartRates: List<HeartRate>? = null
}

class WorkoutLocation {

    @Embedded
    var location: Location? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var locations: List<Location>? = null
}

class WorkoutSummary {

    @Embedded
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    var summary: Summary? = null

}