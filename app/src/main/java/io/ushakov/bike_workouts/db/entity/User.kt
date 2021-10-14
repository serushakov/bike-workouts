package io.ushakov.bike_workouts.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val age: Int,
    val weight: Int,
)

class UserWorkouts {
    @Embedded
    var user: User? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    var workouts: List<Workout>? = null
}
