package com.example.thesis3.dsp

import kotlin.math.abs
import kotlin.math.sqrt

/********************* Wavelet Transform **********************/
object DWT {
    val de_lo: Array<Float> = arrayOf(
        -0.0757657148f, -0.0296355276f, 0.4976186676f,
        0.8037387518f, 0.2978577956f, -0.0992195436f,
        -0.0126039673f, 0.0322231006f
    )

    val de_hi: Array<Float> = arrayOf(
        -0.0322231006f, -0.0126039673f, 0.0992195436f,
        0.2978577956f, -0.8037387518f, 0.4976186676f,
        0.0296355276f, -0.0757657148f
    )

    val re_lo: Array<Float> = arrayOf(
        0.0322231006f, -0.0126039673f, -0.0992195436f,
        0.2978577956f, 0.8037387518f, 0.4976186676f,
        -0.0296355276f, -0.0757657148f
    )

    val re_hi: Array<Float> = arrayOf(
        -0.0757657148f, 0.0296355276f, 0.4976186676f,
        -0.8037387518f, 0.2978577956f, 0.0992195436f,
        -0.0126039673f, -0.0322231006f
    )

    fun thresh_hard(x: Array<Float>, T: Float) {
        for (i in 0 until x.size) {
            if (abs(x[i]) < T) {
                x[i] = 0f
            }
        }
    }

    private fun thresh_soft(x: Array<Float>, T: Float) {
        for (i in 0 until x.size) {
            if (x[i] > T)
                x[i] = x[i] - T
            else if (abs(x[i]) <= T)
                x[i] = 0f
            else if (x[i] < -T)
                x[i] = x[i] + T
        }
    }

    private fun universal_threshold(lenX: Int, c: ComplexArray): Float {
        return sqrt(2f * kotlin.math.ln( lenX.toFloat() )) * median(c.abs()) / 0.6745f
    }

    private fun wdec(x: Array<Float>, N: Int): Pair<Array<ComplexArray>, ComplexArray> {
        var A = x
        val D: Array<ComplexArray> = Array<ComplexArray>(N) { array(arrayOf(0f)) }

        for (i in N-1 downTo  0) {
            D[i] = array(decimate(filtering(A, de_hi), 2))
            A = decimate(filtering(A, de_lo), 2)
        }
        return Pair(D, array(A))
    }

    private fun wrec(D: Array<ComplexArray>, A: ComplexArray, N: Int): Array<Float> {
        var w = A
        val re_low = array(re_lo)
        val re_high = array(re_hi)
        for (i in 0 until N) {
            w = filtering(zerofill(w, 2), re_low) + filtering(zerofill(D[i], 2), re_high)
        }
        return w.real
    }

    fun denoise(x: Array<Float>, N: Int): Array<Float> {
        val x1 = zeropad(x)
        val (D, A) = wdec(x1, N)
        val threshold = universal_threshold(x.size, D.last())

        for (i in 0 until D.size) {
            thresh_soft(D[i].real, threshold)
        }
        return wrec(D, A, N).sliceArray(0 until x.size)
    }
}
/********************* Wavelet Transform **********************/