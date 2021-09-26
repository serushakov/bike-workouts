package io.ushakov.bike_workouts.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity
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