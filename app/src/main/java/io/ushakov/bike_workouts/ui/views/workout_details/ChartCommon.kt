package io.ushakov.bike_workouts.ui.views.workout_details

import android.graphics.Color
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*

fun styleXAxis(xAxis: XAxis, timeMultiplier: Int) {
    xAxis.setDrawAxisLine(false)
    xAxis.valueFormatter = TimeAxisValueFormatter(timeMultiplier)
    xAxis.labelCount = 5
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.textColor = Color.GRAY
    xAxis.axisLineColor = Color.GRAY
}

fun styleYAxis(yAxis: YAxis) {
    yAxis.setDrawAxisLine(false)
    yAxis.textColor = Color.GRAY
    yAxis.axisLineColor = Color.GRAY
}

class TimeAxisValueFormatter(private val multiplier: Int) : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val date = Date(value.toLong() * multiplier)

        return android.text.format.DateFormat.format("HH:mm", date).toString()
    }
}

