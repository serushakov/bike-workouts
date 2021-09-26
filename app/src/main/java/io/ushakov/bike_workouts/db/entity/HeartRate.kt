package io.ushakov.bike_workouts.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            onDelete = CASCADE,
            parentColumns = ["id"],
            childColumns = ["workoutId"]
        )
    ]
)
data class HeartRate(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val workoutId: Long,
    val heartRate: Int
    ) {

    constructor(
        @NotNull workoutId: Long,
        @NotNull heartRate: Int
    ) : this(
        0, workoutId, heartRate
        )
}