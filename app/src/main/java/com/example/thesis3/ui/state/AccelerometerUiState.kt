package com.example.thesis3.ui.state

import com.example.thesis3.data.SENSOR_LIST_SIZE
import com.example.thesis3.data.SENSOR_SAMPLING_RATE
import com.example.thesis3.data.SIGNAL_RESAMPLING_RATE
import com.example.thesis3.data.SensorDataPerTimePeriod
import com.example.thesis3.data.TOTAL_DATA_PER_PERIOD

data class AccelerometerUiState(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,

    var listSize: Int = SENSOR_SAMPLING_RATE,
    var currentIndex: Int = 0,

    //var xList: MutableList<Float> = MutableList(listSize) {0f},
    //var yList: MutableList<Float> = MutableList(listSize) {0f},
    //var zList: MutableList<Float> = MutableList(listSize) {0f},

    //val xSensorDataPerTimePeriod: SensorDataPerTimePeriod = SensorDataPerTimePeriod(),
    //val ySensorDataPerTimePeriod: SensorDataPerTimePeriod = SensorDataPerTimePeriod(),
    //val zSensorDataPerTimePeriod: SensorDataPerTimePeriod = SensorDataPerTimePeriod(),

    val xSensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f},
    val ySensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f},
    val zSensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f},

    //val xSensorDataResized: Array<Float> = Array<Float>(SIGNAL_RESAMPLING_RATE) {0f},
    //val ySensorDataResized: Array<Float> = Array<Float>(SIGNAL_RESAMPLING_RATE) {0f},
    //val zSensorDataResized: Array<Float> = Array<Float>(SIGNAL_RESAMPLING_RATE) {0f},

    val pc1: Array<Float> = arrayOf(),

    val xString: String = "",
    val yString: String = "",
    val zString: String = "",
    val report: String = "",

    // DSP
    var xAvgAmplitude: Float = 0f,
    val pcaSize: Int = 0,
    val cwtSize: Pair<Int, Int> = Pair(0, 0),
    val bpm: Float = 0f,
    )