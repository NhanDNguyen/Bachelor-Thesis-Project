package com.example.thesis3.data

const val SENSOR_LIST_SIZE: Int = 200
const val PERIOD_IN_SECONDS: Int = 10
const val SENSOR_DELAY: Long = 0  // st: 100
const val SENSOR_MAX_VALUE: Float = 10f
const val THRESHOLD = 0.0123f
const val SENSOR_SAMPLING_RATE: Int = 50
const val SIGNAL_RESAMPLING_RATE: Int = 100
const val SAMPLING_PERIOD_US: Int = (1.0/SENSOR_SAMPLING_RATE * 1_000_000).toInt() // sampling rate in microseconds
const val TOTAL_DATA_PER_PERIOD = SENSOR_SAMPLING_RATE * PERIOD_IN_SECONDS
const val RESAMPLED_DATA_LENGTH = TOTAL_DATA_PER_PERIOD * (SIGNAL_RESAMPLING_RATE/SENSOR_SAMPLING_RATE)