package com.example.thesis3.dsp

import kotlin.math.cos
import kotlin.math.sin

/****************************** Window functions ******************************/
enum class Windows {
    BLACKMAN, HAMMING, HANNING
}

fun blackman(L: Int): Array<Float> {
    return Array(L) { 0.42f - 0.5f * cos(2* PI *it/(L-1)) + 0.08f * cos(4* PI *it/(L-1)) }
}

fun hamming(L: Int): Array<Float> {
    return Array(L) { 0.54f - 0.46f * cos(2* PI *it/(L-1)) }
}

fun hanning(L: Int): Array<Float> {
    return Array(L) { 0.5f - 0.5f * cos(2* PI *it/(L-1)) }
}
/****************************** Window functions ******************************/

/****************************** Filter functions ******************************/
fun lowpass(fc: Float, L: Int): Array<Float> {
    val M = (L-1) / 2
    return Array(L) {
        if ((it - M) != 0) {
            sin(2*PI*fc*(it-M)) / (PI*(it-M))
        } else {
            2*fc*1
        }
    }
}
/****************************** Filter functions ******************************/

fun firlComplex(fc: Float, L: Int, window: Windows): List<Complex> {
    val hdn = lowpass(fc, L)
    val wn = when(window) {
        Windows.BLACKMAN -> blackman(L)
        Windows.HANNING -> hanning(L)
        Windows.HAMMING -> hamming(L)
    }
    return List(L) {
        Complex(hdn[it] * wn[it], 0f)
    }
}

fun firl(fc: Float, L: Int, window: Windows): ComplexArray {
    val hdn = lowpass(fc, L)
    val wn = when(window) {
        Windows.BLACKMAN -> blackman(L)
        Windows.HANNING -> hanning(L)
        Windows.HAMMING -> hamming(L)
    }
    return array( Array(L){hdn[it] * wn[it]} )
}

fun firl(fc: Float, L: Int, wn: Array<Float>): ComplexArray {
    val hdn = lowpass(fc, L)
    return array( Array(L){hdn[it] * wn[it]} )
}

fun firl(fc: Float, L: Int, wn: ComplexArray): ComplexArray {
    return array(lowpass(fc, L)) * wn
}