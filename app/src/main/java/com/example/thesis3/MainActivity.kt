package com.example.thesis3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thesis3.Sensor.AccelerometerOperation
import com.example.thesis3.Sensor.AccelerometerSensor
import com.example.thesis3.data.SENSOR_DELAY
import com.example.thesis3.ui.element.AccelerometerViewModel
import com.example.thesis3.ui.theme.Thesis3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: AccelerometerViewModel = viewModel()
            val accelerometerSensor = AccelerometerSensor(this)
            val accelerometerUiState by viewModel.accelerometerUiState.collectAsState()

            Thesis3Theme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AccelerometerOperation(
                        accelerometerSensor = accelerometerSensor,
                        viewModel = viewModel,
                        //delay = SENSOR_DELAY
                        delay = 0
                    )

                    /*Text(
                        text = accelerometerUiState.xString
                    )

                    Text(
                        text = accelerometerUiState.yString
                    )

                    Text(
                        text = accelerometerUiState.zString
                    )*/
                    Text(
                        text = "PCA size: ${accelerometerUiState.pcaSize}"
                    )
                    Text(
                        text = "CWT size: (${accelerometerUiState.cwtSize.first}, ${accelerometerUiState.cwtSize.second})"
                    )
                    LinearChart1(
                        modifier = Modifier
                            .background(Color.Gray)
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(12.dp),
                        data = accelerometerUiState.pc1,
                        color = Color.Black
                    )

                    /*LinearChart1(
                        modifier = Modifier
                            .background(Color.Gray)
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(12.dp),
                        data = accelerometerUiState.xSensorDataPerTimePeriod,
                        color = Color.Red
                    )
                    LinearChart1(
                        modifier = Modifier
                            .background(Color.Gray)
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(12.dp),
                        data = accelerometerUiState.ySensorDataPerTimePeriod,
                        color = Color.Green
                    )
                    LinearChart1(
                        modifier = Modifier
                            .background(Color.Gray)
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(12.dp),
                        data = accelerometerUiState.zSensorDataPerTimePeriod,
                        color = Color.Blue
                    )*/

                    /*LinearChart(
                        modifier = Modifier
                            .background(Color.Gray)
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(12.dp),
                        sensorDataPerTimePeriod = accelerometerUiState.xSensorDataPerTimePeriod,
                        color = Color.Red
                    )*/

                }
            }
        }
    }
}

