package io.ushakov.bike_workouts.ui.views.workout_details

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.ushakov.bike_workouts.db.entity.Location
import kotlin.math.roundToInt

const val ELEVATION_TIME_DIVIDER = 6000

@Composable
fun ElevationGraph(locations: List<Location>, modifier: Modifier = Modifier) {
    AndroidView(factory = { context -> LineChart(context) }, modifier) { lineChart ->
        lineChart.description = null
        lineChart.legend.isEnabled = false
        styleXAxis(lineChart.xAxis, ELEVATION_TIME_DIVIDER)
        styleYAxis(lineChart.getAxis(YAxis.AxisDependency.LEFT))
        styleYAxis(lineChart.getAxis(YAxis.AxisDependency.RIGHT))

        val data = getEntries(locations)

        val dataSet = LineDataSet(data, null)
        dataSet.setColor(Color.GREEN, 100)
        dataSet.lineWidth = 5f
        dataSet.setDrawCircles(false)
        dataSet.setDrawFilled(true)
        dataSet.fillDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.GREEN, Color.TRANSPARENT))
        dataSet.label = null
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }
}

fun roundToPointHalf(value: Float) = (value * 2).roundToInt() / 2f

fun createDateElevationPair(location: Location) =
    location.timestamp.time.div(ELEVATION_TIME_DIVIDER)
        .toFloat() to roundToPointHalf(location.elevation)

fun getEntries(locations: List<Location>): List<Entry> {
    val processedElevations = mutableListOf<Pair<Float, Float>>()

    val first = locations.first()
    val last = locations.last()

    // Manually adding first item so that graph starts at start time
    processedElevations += createDateElevationPair(first)

    // First distinctBy filters out all duplicate elevation values
    // Second distinctBy filters out all duplicate timestamp values
    // This way we get 1 pair of unique timestamp to elevation pair for a clean graph
    processedElevations +=
        locations.map { createDateElevationPair(it) }.distinctBy { it.second }.distinctBy { it.first }.toMutableList()

    // Manually adding last item so that graph ends at end time, and graph goes from edge to edge
    processedElevations += last.timestamp.time.div(ELEVATION_TIME_DIVIDER)
        .toFloat() to roundToPointHalf(last.elevation)

    return processedElevations.map { Entry(it.first, it.second) }
}