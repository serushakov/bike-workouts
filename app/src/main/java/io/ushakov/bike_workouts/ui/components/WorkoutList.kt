package io.ushakov.bike_workouts.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.ushakov.bike_workouts.db.entity.WorkoutSummary
import io.ushakov.bike_workouts.util.debounce
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.roundToInt

@Composable
fun WorkoutList(
    workoutSummaryList: List<WorkoutSummary>,
    onSelect: (WorkoutSummary) -> Unit,
    onDelete: (workoutId: Long) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)) {
        itemsIndexed(workoutSummaryList.filter { it.summary != null && it.workout != null }) { index, workoutSummary ->
            DeletableCard(
                onDelete = {
                    onDelete(workoutSummary.workout?.id!!)
                },
            ) {
                WorkoutColumnItem(
                    workoutSummary.workout!!.startAt,
                    workoutSummary.summary!!.distance,
                    workoutSummary.summary!!.kiloCalories
                ) { onSelect(workoutSummary) }

                if (index != workoutSummaryList.size - 1) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DeletableCard(
    onDelete: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val cardOffset = 170f
    var offsetX by remember { mutableStateOf(0f) }
    var targetOffset by remember { mutableStateOf<Float?>(null) }

    val scope = rememberCoroutineScope()

    val determineEndState = debounce<Float>(20, scope) { offset ->
        targetOffset = if (offset < -cardOffset / 2) -cardOffset else 0f
    }

    val offset by animateFloatAsState(targetOffset ?: offsetX)

    Box(Modifier.fillMaxWidth()) {
        IconButton(onClick = onDelete,
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)) {
            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colors.error)
        }
        Row(
            modifier = Modifier
                .offset { IntOffset((offset).roundToInt(), 0) }
                .pointerInput("drag") {
                    detectHorizontalDragGestures { change, dragAmount ->
                        targetOffset = null
                        offsetX = max(min(offsetX + dragAmount, 0f), -cardOffset)

                        determineEndState(offsetX)
                        change.consumePositionChange()
                    }
                },
            content = content
        )
    }

}