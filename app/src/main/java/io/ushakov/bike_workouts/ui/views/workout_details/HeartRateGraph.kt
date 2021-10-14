package io.ushakov.bike_workouts.ui.views.workout_details

import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import io.ushakov.bike_workouts.db.entity.HeartRate
import java.lang.Integer.min

const val HR_TIME_DIVIDER = 60000

@Composable
fun HeartRateGraph(heartRates: List<HeartRate>, modifier: Modifier = Modifier) {
    AndroidView(factory = { context -> ScatterChart(context) }, modifier) { scatterChart ->
        scatterChart.description = null
        scatterChart.legend.isEnabled = false

        styleXAxis(scatterChart.xAxis, HR_TIME_DIVIDER)
        styleYAxis(scatterChart.getAxis(YAxis.AxisDependency.LEFT))
        styleYAxis(scatterChart.getAxis(YAxis.AxisDependency.RIGHT))

        val data = heartRates.map {
            it.timestamp.time.div(HR_TIME_DIVIDER).toFloat() to it.heartRate.toFloat()
        }

        val optimizedData = optimizeHeartRateDataSet(data,
            when (data.size) {
                in 0..1000 -> 1
                in 1000..9999 -> 4
                else -> 10
            }
        )

        val dataSet =
            ScatterDataSet(optimizedData.map { Entry(it.first, it.second) }, null)
        dataSet.setColor(Color.RED, 100)

        val lineData = ScatterData(dataSet)
        scatterChart.data = lineData
        scatterChart.invalidate()
    }
}

/**
 * If there are too many data points on the graph, UI starts to lag.
 *
 * This function is averaging every `sizeDivider` consecutive heartrate readings,
 * which results in an array `sizeDivider` times smaller than input
 */
fun optimizeHeartRateDataSet(
    data: List<Pair<Float, Float>>,
    sizeDivider: Int,
): List<Pair<Float, Float>> {
    if (sizeDivider == 1) return data
    val optimizedData = mutableListOf<Pair<Float, Float>>()

    if (data.isEmpty()) return optimizedData

    for (i in 0..data.size step sizeDivider) {
        val items = data.slice(i..min(i + sizeDivider, data.lastIndex))

        optimizedData.add(items.first().first to items.map { it.second }.average().toFloat())
    }

    optimizedData.add(data.last())

    return optimizedData
}