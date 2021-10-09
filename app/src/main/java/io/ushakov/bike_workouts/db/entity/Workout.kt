package io.ushakov.bike_workouts.db.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
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
    var id: Long = 0,
    val userId: Long,
    val title: String,
    val type: String,
    val startAt: Date,
    var finishAt: Date? = null,
    var isActive: Boolean = true,
) {

    fun clone() = Workout(
        id = this.id,
        userId = this.userId,
        title = this.title,
        type = this.type,
        startAt = this.startAt,
        finishAt = this.finishAt,
        isActive = this.isActive
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