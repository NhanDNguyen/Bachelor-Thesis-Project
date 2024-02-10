package com.example.thesis3

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thesis3.data.SensorDataPerTimePeriod


@Composable
fun LinearChart(
    modifier: Modifier = Modifier,
    sensorDataPerTimePeriod: SensorDataPerTimePeriod,
    xValueSize: Int = 100,
    color: Color = Color.Blue
) {
    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            android.graphics.Color.WHITE
            Paint.Align.CENTER
            textSize = density.run {12.sp.toPx() }
        }
    }
    val textInterval = 0 / 30f
    var nextTextInterval = 0f

    val yValues = listOf(10f, 5f, 0f, -5f, -10f)
    val paddingSpace: Dp = 100.dp

    //if (sensorDataPerTimePeriod.sensorData.isEmpty()) return

    Canvas(modifier = modifier) {
        // Total number of transactions.
        val totalRecords = sensorDataPerTimePeriod.sensorData.size

        // Maximum distance between dots (transactions)
        val lineDistance = 10f

        // Canvas height
        val cHeight = size.height

        val yAxisSpace = cHeight / yValues.size

        // Add some kind of a "Padding" for the initial point where the line starts.
        val paddingDistance = paddingSpace.toPx() / 2f
        var currentLineDistance = paddingDistance

        val mid = cHeight / 2
        val max = mid - (10f/cHeight * mid * 20f)
        val min = mid + (10f/cHeight * mid * 20f)
        val interval = (max * 2) / (yValues.size + 0.2f)
        // Draw y axis values
        for (i in yValues.indices) {
            drawContext.canvas.nativeCanvas.drawText(
                "${yValues[i]}",
                0f,
                max + (i * interval),
                textPaint
            )
        }

        // Draw x axis line
        drawLine(
            start = Offset(
                x = currentLineDistance,
                y = min + 30f
            ),
            end = Offset(
                x = size.width,
                y = min + 30f
            ),
            color = Color.Black,
            strokeWidth = Stroke.DefaultMiter
        )

        // Draw y axis line
        drawLine(
            start = Offset(
                x = currentLineDistance,
                y = min + 30f
            ),
            end = Offset(
                x = currentLineDistance,
                y = max - 30f
            ),
            color = Color.Black,
            strokeWidth = Stroke.DefaultMiter
        )

        sensorDataPerTimePeriod.sensorData.forEachIndexed { index, data ->
            if (totalRecords >= index + 2) {
                drawLine(
                    start = Offset(
                        x = currentLineDistance,
                        y = calY(
                            canvasHeight = cHeight,
                            nextSensorValue = 0f,
                            currentSensorValue = data
                        )
                    ),
                    end = Offset(
                        x = currentLineDistance + lineDistance,
                        y = calY(
                            canvasHeight = cHeight,
                            nextSensorValue = 0f,
                            currentSensorValue = sensorDataPerTimePeriod.sensorData[index + 1],
                        )
                    ),
                    color = color,
                    strokeWidth = Stroke.DefaultMiter
                )

                if (index == nextTextInterval.toInt()) {
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            index.toString(),
                            currentLineDistance,
                            cHeight,
                            textPaint
                        )
                    }
                    nextTextInterval += 20f
                }

            }
            if (currentLineDistance >= size.width) {
                sensorDataPerTimePeriod.sensorData = sensorDataPerTimePeriod.sensorData.sliceArray(index until
                    sensorDataPerTimePeriod.sensorData.size)
                currentLineDistance = paddingDistance
                nextTextInterval = 0f
            } else {
                currentLineDistance += lineDistance
            }
        }
    }
}

private fun calY(
    canvasHeight: Float,
    nextSensorValue: Float,
    currentSensorValue: Float
): Float {
    val mid = canvasHeight / 2
    val normalizeNextValue = nextSensorValue / canvasHeight
    val normalizeCurrentValue = (currentSensorValue / canvasHeight)

    return mid - (normalizeCurrentValue * mid * 20f)

}