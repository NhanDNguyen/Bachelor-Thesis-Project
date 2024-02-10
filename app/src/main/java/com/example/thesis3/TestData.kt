package com.example.thesis3

import com.example.thesis3.dsp.DWT
import com.example.thesis3.dsp.PI
import com.example.thesis3.dsp.arange
import com.example.thesis3.dsp.array
import com.example.thesis3.dsp.cov
import com.example.thesis3.dsp.normalize
import com.example.thesis3.dsp.preprocessing
import com.example.thesis3.dsp.resample
import kotlin.math.sin

fun add(a: Array<Float>, b: Array<Float>): Array<Float> {
    return Array<Float>(a.size) {
        a[it] + b [it]
    }
}

fun sin_gen(A: Float, f0: Float, fs: Float, t: Float, fii: Float, plot: Boolean=false): Array<Float> {
    val Ts = 1 / fs
    val n = arange(0f, t+Ts, Ts)
    val s = Array<Float>(n.size) {
        A * sin(2* PI *f0*n[it] + fii)
    }
    return s
}



val fn = sin_gen(5f, 5f, 199.0f, 1.0f, 0f)
val noise = Array<Float>(200) {
    (-3 until 3).random() / 1f
}
val fn_noisy = add(fn, noise)
val fn_noisy_resampled = resample(fn_noisy, 200, 100)
//val fn_denoise = DWT.denoise(fn_noisy_resampled, 7)

val fn_denoise = normalize(fn_noisy_resampled)

val a = arrayOf(1f, 2f, 4f, 5f, 6f, 7f, 8f, 9f)
val b = arrayOf(0f, 2f, 5f, 10f, 11f, 9f, 8f, 1f)
val cov = cov(a, b)
