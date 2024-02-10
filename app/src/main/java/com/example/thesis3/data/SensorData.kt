package com.example.thesis3.data

data class SensorDataPerTimePeriod(
    var maxValue: Double = -999999.0,
    var sensorData: Array<Float> = Array<Float>(SENSOR_LIST_SIZE) {0f},
)


data class SensorRate (
    val timeStamp: Long,
    val sensorDataPerTimePeriodValue: Double,
)