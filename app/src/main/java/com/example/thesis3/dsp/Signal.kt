package com.example.thesis3.dsp

import com.example.thesis3.data.RESAMPLED_DATA_LENGTH
import com.example.thesis3.data.SENSOR_SAMPLING_RATE
import com.example.thesis3.data.SIGNAL_RESAMPLING_RATE
import com.example.thesis3.data.THRESHOLD
import com.example.thesis3.data.TOTAL_DATA_PER_PERIOD
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow


fun filtering(x: Array<Float>, h: Array<Float>): Array<Float> {
    val y = fftconvolve(x, h, mode="full")
    val L = (y.size - x.size)/2
    return y.slice(L, y.size-L)
}

fun filtering(x: ComplexArray, h: ComplexArray): ComplexArray {
    val y = fftconvolve(x, h, mode="full")
    val L =  (y.size() - x.size()) /2
    return y.slice(L, y.size()-L)
}

val wn = firl(0.25f, 41, Windows.BLACKMAN).real
fun resample(x: Array<Float>, inputFs: Int, outputFs: Int): Array<Float> {
    /*val (p, q) = reduceFraction(inputFs, outputFs) // p -> decimate factor, q -> interpolate factor
    val maxpq = max(p, q)
    val fc = 1f / maxpq
    val n = 10
    val order = 2 * n * maxpq
    val y_lpf = filtering(  zerofill(x, q), firl(0.1f, order, Windows.HAMMING) )
    return decimate(y_lpf, p)*/
    val s = zerofill(x, 2)
    val x_pad = s + s + s
    val y = fftconvolve(x_pad, wn, mode = "full")*2f
    val L = ((y.size - s.size) / 2f).toInt()
    //return y.sliceArray(L until y.size-L-1)
    return y.sliceArray(L until y.size-L)
}

//val x = arange(start = 1f, stop = SIGNAL_RESAMPLING_RATE.toFloat()+1)
val x = arange(start = 1f, stop = (SIGNAL_RESAMPLING_RATE*10).toFloat()+1)
val ux = mean(x)
val ux2 = x.map { it*it }.sum() / x.size
val Sxx = x.map { (it - ux).pow(2) }.sum()
val Sxx2 = x.map { (it-ux)*(it.pow(2)-ux2) }.sum()
val Sx2x2 = x.map { (it.pow(2)- ux2).pow(2) }.sum()

fun normalize(y: Array<Float>): Array<Float> {
    var max = -99999.0f
    var Sxy = 0f
    var Sx2y = 0f
    val uy = mean(y)
    for (i in 0 until y.size) {
        Sxy += (x[i]-ux)*(y[i] - uy)
        Sx2y += (x[i].pow(2)-ux2)*(y[i]-uy)
    }
    val d = Sxx*Sx2x2 - Sxx2.pow(2)
    val a = (Sx2y*Sxx - Sxy*Sxx2) / d
    val b = (Sxy*Sx2x2 - Sx2y*Sxx2) / d
    val c = uy - b*ux - a*ux2
    var value = 0f
    val y_p: Array<Float> = Array<Float>(y.size) {
        value = y[it] - (a*x[it].pow(2)+b*x[it]+c)
        if (abs(value) > max) {
            max = abs(value)
        }
        value
    }
    y_p.forEachIndexed() {
            it, v -> y_p[it] = v / max
    }
    return y_p
}

fun averageAmplitude(x: Array<Float>): Float {
    return (x.sum() / x.size)
}

fun upSample(yp: Array<Float>, inputFs: Float, outputFs: Float): Array<Float> {
    val scale = outputFs / inputFs
    val n = (yp.size * scale).toInt()
    val y = interpolateLinear(
        x = linspace(0f, 1f, n, endpoint = true),
        xp = linspace(0f, 1f, x.size, endpoint = true),
        yp = yp
    )
    return y
}

// Up sample
val _XP = linspace(0f, 1f, TOTAL_DATA_PER_PERIOD, endpoint = true)
val _X = linspace(0f, 1f, RESAMPLED_DATA_LENGTH, endpoint = true)
fun upSample(yp: Array<Float>) = interpolateLinear(_X, _XP, yp)

fun preprocessing(s: Array<Float>): Array<Float> {
    // Filter out week signal
    //if (averageAmplitude(x) < THRESHOLD)
    //   return Array<Float>(x.size) {0f}

    // Resampling to Fs=100Hz
    /*var xResampled = resample(
        x = array(x),
        inputFs = SENSOR_SAMPLING_RATE,
        outputFs =  SIGNAL_RESAMPLING_RATE
    ).real*/
    /*var sResampled = resample(
        x = s,
        inputFs = SENSOR_SAMPLING_RATE*10,
        outputFs = SIGNAL_RESAMPLING_RATE*10
    )*/

    var sResampled = upSample(s)

    // Normalization
    sResampled = normalize(sResampled)

    // Denoising
    return DWT.denoise(sResampled, 7)
}