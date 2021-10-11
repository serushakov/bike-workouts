package io.ushakov.bike_workouts.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["workoutId"]
        )
    ]
)
data class Duration(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(index = true)
    val workoutId: Long,
    val startAt: Date,
    var stopAt: Date?
) /*{

    constructor(
        @NotNull workoutId: Long,
        @NotNull startAt: Date,
        @Nullable stopAt: Date?
    ) : this(
        0, workoutId, startAt, stopAt
    )
}*/