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


@Composable
fun LinearChart1(
    modifier: Modifier = Modifier,
    data: Array<Float>,
    xValueSize: Int = 100,
    color: Color = Color.Blue
) {
    Canvas(modifier = modifier) {
        val totalRecords = data.size

        // Maximum distance between dots (transactions)
        val lineDistance = size.width / (totalRecords + 1)

        // Canvas height
        val cHeight = size.height

        // Add some kind of a "Padding" for the initial point where the line starts.
        var currentLineDistance = 0F + lineDistance

        data.forEachIndexed { index, value ->
            if (totalRecords >= index + 2) {
                drawLine(
                    start = Offset(
                        x = currentLineDistance,
                        y = calculateYCoordinate(
                            canvasHeight = cHeight,
                            nextSensorValue = 0f,
                            currentSensorValue = value
                        )
                    ),
                    end = Offset(
                        x = currentLineDistance + lineDistance,
                        y = calculateYCoordinate(
                            canvasHeight = cHeight,
                            nextSensorValue = 0f,
                            currentSensorValue = data[index + 1],
                        )
                    ),
                    color = color,
                    strokeWidth = Stroke.DefaultMiter
                )
            }
            currentLineDistance += lineDistance
        }
    }
}

private fun calculateYCoordinate(
    canvasHeight: Float,
    nextSensorValue: Float,
    currentSensorValue: Float
): Float {
    val mid = canvasHeight / 2
    val normalizeNextValue = nextSensorValue / canvasHeight
    val normalizeCurrentValue = (currentSensorValue / canvasHeight)

    return mid - (normalizeCurrentValue * mid * 20f)*5f
}