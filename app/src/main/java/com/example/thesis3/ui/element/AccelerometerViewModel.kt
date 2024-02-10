package com.example.thesis3.ui.element

import androidx.lifecycle.ViewModel
import com.example.thesis3.data.PERIOD_IN_SECONDS
import com.example.thesis3.data.SENSOR_LIST_SIZE
import com.example.thesis3.data.SENSOR_SAMPLING_RATE
import com.example.thesis3.data.SIGNAL_RESAMPLING_RATE
import com.example.thesis3.data.SensorDataPerTimePeriod
import com.example.thesis3.data.TOTAL_DATA_PER_PERIOD
import com.example.thesis3.dsp.ComplexArray
import com.example.thesis3.dsp.PCA
import com.example.thesis3.dsp.averageAmplitude
import com.example.thesis3.dsp.beatSeperation
import com.example.thesis3.dsp.cwt
import com.example.thesis3.dsp.ln
import com.example.thesis3.dsp.mean
import com.example.thesis3.dsp.median
import com.example.thesis3.dsp.pow
import com.example.thesis3.dsp.preprocessing
import com.example.thesis3.dsp.size
import com.example.thesis3.dsp.times
import com.example.thesis3.ui.state.AccelerometerUiState
import com.example.thesis3.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

enum class Status {
    COLLECTING, PROCESSING
}

class AccelerometerViewModel(): ViewModel() {
    private val _accelerometerUiState = MutableStateFlow(AccelerometerUiState())
    val accelerometerUiState: StateFlow<AccelerometerUiState> = _accelerometerUiState.asStateFlow()
    var status: Status = Status.COLLECTING

    init {
        _accelerometerUiState.value.listSize = SENSOR_SAMPLING_RATE
    }

    // sensor data
    private var sensorIndex = 0
    private val xSensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f}
    private val ySensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f}
    private val zSensorDataPerTimePeriod: Array<Float> = Array<Float>(TOTAL_DATA_PER_PERIOD) {0f}

    private var xSensorDataResized: Array<Float> = arrayOf()
    private var ySensorDataResized: Array<Float> = arrayOf()
    private var zSensorDataResized: Array<Float> = arrayOf()

    private var signalDetails: Array<ComplexArray> = arrayOf()

    private var pcaPerPeriod: Array<Float> = arrayOf()
    var tempPca: Array<Float> = arrayOf()


    var index = 0
    private var maxX = -99999f
    private var maxY = -99999f
    private var maxZ = -99999f
    private var minX = 99999f
    private var minY = 99999f
    private var minZ = 99999f
    private val xDataPerPeriod: Array<Float> = Array(50) { 0f }
    private val yDataPerPeriod: Array<Float> = Array(50) { 0f }
    private val zDataPerPeriod: Array<Float> = Array(50) { 0f }
    private var xValue = 0f
    private var yValue = 0f
    private var zValue = 0f
    fun updateAxesValues(x: Float, y: Float, z: Float) {
        runBlocking(Dispatchers.Default) {
            launch {
                //xValue += x*x
                xDataPerPeriod[index] = x
                if (maxX < abs(x))
                    maxX = x
                if (minX > abs(x))
                    minX = x
            }
            launch {
                //yValue += y*y
                yDataPerPeriod[index] = y
                if (maxY < abs(y))
                    maxY = y
                if (minY > abs(y))
                    minY = y
            }
            launch {
                //zValue += z*z
                zDataPerPeriod[index] = z
                if (maxZ < abs(z))
                    maxZ = z
                if (minZ > abs(z))
                    minZ = z
            }
        }

        if (index == 49) {
            val xAvg = xDataPerPeriod.sum()/50
            val yAvg = yDataPerPeriod.sum()/50
            val zAvg = zDataPerPeriod.sum()/50
            //val xAvg = sqrt(xDataPerPeriod.pow(2).sum()/50)
            //val yAvg = sqrt(yDataPerPeriod.pow(2).sum()/50)
            //val zAvg = sqrt(zDataPerPeriod.pow(2).sum()/50)
            //var msg: String = ""
            /*if (xAvg < abs((maxX-minX)))
                msg = "flat on surface"
            else
                msg = "not flat on surface"*/
            _accelerometerUiState.update { currentState ->
                currentState.copy(
                    //xString = "%.7f | %.7f".format( (xValue/50), maxX ),
                    //yString = "%.7f | %.7f".format( (yValue/50), maxY ),
                    //zString = "%.7f | %.7f".format( (zValue/50), maxZ ),
                    /*report = "values       |       min       |       max       |      max-min\n" +
                            "%.7f | %.7f | %.7f | %.7f\n".format( (xValue/50), minX, maxX, maxX-minX ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( (yValue/50), minY, maxY, maxY-minY ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( (zValue/50), minZ, maxZ, maxZ-minZ )
                )*/
                    /*report = "values       |       min       |       max       |      max+min\n" +
                            "%.7f | %.7f | %.7f | %.7f\n".format( xAvg, minX, maxX, (maxX+minX)/2 ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( yAvg, minY, maxY, (maxY+minY)/2 ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( zAvg, minZ, maxZ, (maxZ+minZ)/2 )*/

                    report = "values       |       min       |       max       |      max+min\n" +
                            "%.7f | %.7f \n".format( xAvg, abs(xAvg)/ abs(maxX) ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( yAvg, minY, maxY, (maxY+minY)/2 ) +
                            "%.7f | %.7f | %.7f | %.7f\n".format( zAvg, minZ, maxZ, (maxZ+minZ)/2 )

                )
            }
            maxX = -99999f
            maxY = -99999f
            maxZ = -99999f
            minX = 99999f
            minY = 99999f
            minZ = 99999f
            xValue = 0f
            yValue = 0f
            zValue = 0f
        }

        index = (index+1) % 50
    }
    

    // update ui state of sensor data
    fun updateAxesLists(x: Float, y: Float, z: Float) {
        // check if the list is at the maximum sizes
        if (status == Status.PROCESSING) {
            _preprocessing()
            _signalPrecomposition()
            _cwt()
            // Reset array index
            sensorIndex = 0
            status = Status.COLLECTING
        }
        // add sensor value to lists
        if (status == Status.COLLECTING)
            addSensorData(x, y, z)
    }

    private fun _signalPrecomposition() {
        pcaPerPeriod = PCA(xSensorDataResized, ySensorDataResized, zSensorDataResized)
        _accelerometerUiState.update {
            it.copy(
                pc1 = pcaPerPeriod,
                pcaSize = pcaPerPeriod.size
            )
        }
    }

    private fun _preprocessing() {
        runBlocking(Dispatchers.Default) {
            launch {
                //xSensorDataResized = preprocessing(xSensorDataPerTimePeriod)
                xSensorDataResized = preprocessing(xSensorDataPerTimePeriod)
            }
            launch {
                //ySensorDataResized = preprocessing(ySensorDataPerTimePeriod)
                ySensorDataResized = preprocessing(ySensorDataPerTimePeriod)
            }
            launch {
                //zSensorDataResized = preprocessing(zSensorDataPerTimePeriod)
                zSensorDataResized = preprocessing(zSensorDataPerTimePeriod)
            }
        }
    }

    private fun _cwt() {
        runBlocking(Dispatchers.Default) {
            launch {
                signalDetails = cwt(pcaPerPeriod)
                _accelerometerUiState.update {
                    it.copy(
                        cwtSize = Pair(signalDetails.size, signalDetails[0].size())
                    )
                }
            }
        }
    }

    private fun resetAxesLists() {
        /*runBlocking(Dispatchers.Default) {
            launch { xSensorDataPerTimePeriod.fill(0f) }
            launch { ySensorDataPerTimePeriod.fill(0f) }
            launch { zSensorDataPerTimePeriod.fill(0f) }
        }*/
        sensorIndex = 0
        status = Status.COLLECTING
    }

    // Testing functions
    private fun addSensorData(x: Float, y: Float, z: Float) {
        runBlocking(Dispatchers.Default) {
            launch {
                xSensorDataPerTimePeriod[sensorIndex] = x
            }
            launch {
                ySensorDataPerTimePeriod[sensorIndex] = y
            }
            launch {
                zSensorDataPerTimePeriod[sensorIndex] = z
            }
        }
        ++sensorIndex
        if (sensorIndex == TOTAL_DATA_PER_PERIOD) {
            status = Status.PROCESSING
        }
    }
}