package com.example.thesis3.Sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.thesis3.ui.element.AccelerometerViewModel
import kotlinx.coroutines.delay


class AccelerometerSensor(
    context: Context,
): AndroidSensor(
    context = context,
    sensorFeature = PackageManager.FEATURE_SENSOR_ACCELEROMETER,
    sensorType = Sensor.TYPE_ACCELEROMETER
)

@Composable
fun AccelerometerOperation(
    accelerometerSensor: AccelerometerSensor,
    viewModel: AccelerometerViewModel,
    delay: Long = 0
) {
    LaunchedEffect(Unit) {
        delay(delay)
        accelerometerSensor.startListening()
        accelerometerSensor.setOnSensorValuesChangedListener {values ->
            viewModel.updateAxesLists(
                x = values[0],
                y = values[1],
                z = values[2]
            )
        }
    }

    /*LaunchedEffect(Unit) {
        delay(delay)
        accelerometerSensor.startListening()
        accelerometerSensor.setOnSensorValuesChangedListener {values ->
            viewModel.updateAxesValues(
                x = values[0],
                y = values[1],
                z = values[2]
            )
        }
    }*/
}