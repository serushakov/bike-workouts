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
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val workoutId: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Float,
    val elevation: Float
) {

    constructor(
        @NotNull workoutId: Long,
        @NotNull latitude: Double,
        @NotNull longitude: Double,
        @NotNull speed: Float,
        @NotNull elevation: Float
    ) : this(
        0, workoutId, latitude, longitude, speed, elevation
    )
}