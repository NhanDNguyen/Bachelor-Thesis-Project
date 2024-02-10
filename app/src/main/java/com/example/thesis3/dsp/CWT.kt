package com.example.thesis3.dsp

import com.example.thesis3.data.RESAMPLED_DATA_LENGTH
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

fun morse(
    s: Array<Float>,
    w: Array<Array<Float>>,
    w0: Float,
    g: Float,
    b: Float,
    k: Float=0f
): Array<ComplexArray> {
    val nw = w.size
    val ns = s.size
    val r =(2*b+1)/g
    val c = r - 1
    val WS = w * s
    val a: Array<Array<Float>> = if (b != 0f) {
        2* sqrt(exp(gammaln(r)+gammaln(k+1)-gammaln(k+r))).toFloat() * exp(b*ln(WS/w0)-WS.pow(g)+w0.pow(g))
    } else {
        2f * exp(-1f*WS.pow(g))
    }
    a[0] = 0.5f * a[0]
    a.forEach {
        it.forEachIndexed { index, v ->
            if (v.isNaN()) {
                it[index] = 0f
            }
        }
    }
    val tempArr = linspace(0f, (nw-1).toFloat(), nw)
    var psihat = array(a * laguerre(2f*WS.pow(g), k, c))
    psihat *= ( reshape(expComplex(PI.toFloat()*tempArr), rows = tempArr.size, columns = 1) * array(arrayOfSpecificNumber(ns, value = 1f)))
    reassignArray(
        psihat,
        Triple(1, psihat.size, 1),
        s.compareTo(0f, "<"),
        Triple(-1, 0, -1),
        s.compareTo(0f, "<")
    )
    reassignArray(
        psihat,
        Triple(0, psihat.size, 1),
        s.compareTo(0f, "<"),
        psihat.getValues(Triple(0, psihat.size, 1), s.compareTo(0f, "<")).conjugateAndReturn()
    )
    return psihat
}

fun getOmega(n: Int): Array<Array<Float>> {
    val omega = arrayOfSpecificNumber(n, value = 0f)
    for (i in 1 until n/2+1) {
        omega[i] = (2*PI/n * i).toFloat()
    }
    for (i in n/2+1 until n) {
        omega[i] = -(2*PI/n * (n-i)).toFloat()
    }
    return reshape(omega, rows = omega.size, columns = 1)
}

fun getMorseCenterFreq(g: Float, b: Float): Float {
    return if (b!=0f) exp((1/g)*(ln(b)- ln(g))) else ln(2f).pow(1f/g)
}

fun getMorseSigma(g: Double, b: Double): Pair<Float, Float> {
    val a = (2/g) * (ln(g) - ln(2*b));
    val sigmafreq = (sqrt(exp(a + gammaln((2*b+3)/g) - gammaln((2*b+1)/g)) - exp(a + 2*gammaln((2*b+2)/g) - 2*gammaln((2*b+1)/g))));

    val c1 = (2/g)*ln(2*b/g) + 2*ln(b) + gammaln((2*(b-1)+1)/g) - gammaln((2*b+1)/g);
    val c2 = ((2-2*g)/g)*ln(2.0) + (2/g)*ln(b/g) + 2*ln(g) + gammaln((2*(b-1+g)+1)/g) - gammaln((2*b+1)/g);
    val c3 = ((2-g)/g)*ln(2.0) + (1+2/g)*ln(b) + (1-2/g)*ln(g) + ln(2.0) + gammaln((2*(b-1+g/2)+1)/g) - gammaln((2*b+1)/g);
    val sigmatime = (sqrt(exp(c1) + exp(c2) - exp(c3)));
    return Pair(sigmafreq.toFloat(), sigmatime.toFloat())
}

fun getScales(n: Int,
              s0: Float,
              no: Float,
              nv: Int,
              g: Float,
              b: Float,
              freqLimits: Array<Float>,
              fs: Float
): Triple<Array<Float>, Float, Float> {
    if (freqLimits.isNotEmpty()) {
        val w0 = exp((1/g) * ln(b/g))
        val wLow = freqLimits[0] / fs * 2 * PI.toFloat()
        val wHigh = freqLimits[1] / fs * 2 * PI.toFloat()
        val octaves = log2(wHigh/wLow)
        val j = arange(0f, octaves*nv)
        val scales = (w0/wHigh) * 2f.pow(j/nv)
        return Triple(scales, s0, no)
    }

    var a = 0
    var s0_ = 0f
    var no_ = 0f
    if (s0 == 0f) {
        a = 1
        val testomegas = linspace(0f, 12* PI.toFloat(), 1001)
        val temp = ln(testomegas.pow(b)) + (-1f*testomegas.pow(g)) - ln(a/2f) + (b/g)*(1f+ln(g/b))
        val omega = testomegas.getValues(Triple(0, testomegas.size, 1), temp.compareTo(0f, ">")).last()
        s0_ = min(2f, omega/ PI.toFloat())
    }
    val (sigmafreq, sigmatime) = getMorseSigma(g.toDouble(), b.toDouble())
    var hi = floor(n/(2f*sigmatime*s0_))
    if (hi<=1f) {
        hi = floor(n/2f)
    }
    hi = floor(nv*log2(hi))
    if (no == 0f) {
        no_ = hi/nv
    } else{
        hi = min(nv*no_, hi)
    }
    val octaves = (1f/nv)*arange(0f, hi+1f)
    val scales = s0_*2f.pow(octaves)
    return Triple(scales, s0_, no_)
}

// CWT for real values
fun cwt(x_: Array<Float>,
        fs: Float,
        g: Float=3f,
        b_: Float=10f,
        k: Int=0,
        no_: Float=0f,
        nv_: Int=10,
        pad: Boolean=true,
        freqLimits: Array<Float> = emptyArray()
): Triple<Array<ComplexArray>, Array<Float>, Array<Float>> {

    var b = max(b_, ceil(3/g))
    b = min(b, (120/g.toInt()).toFloat())
    var nv = 2* ceil(nv_/2.0).toInt()
    nv = max(nv, 4)
    nv = min(nv, 48)

    var x = x_.reversedArray() - mean(x_)
    val norig: Int = x.size

    var npad: Int = 0
    if (pad) {
        npad = norig/2
        val start = x.slice(0, npad,1).reversedArray()
        val end = x.slice(npad, x.size, 1).reversedArray()
        x = arrayOf(*start,  *x,  *end)
    }
    val n = x.size
    val (s, s0, no) = getScales(norig, 0f, no_, nv, g, b,freqLimits, fs)
    val w = getOmega(n)
    val wc = getMorseCenterFreq(g, b)
    val psihat = morse(s, w, wc, g, b, k.toFloat())
    //val (sigmafreq, sigmatime) = getMorseSigma(g.toDouble(), b.toDouble())
    //val coival = 2*PI / (sigmatime*wc)
    val xhat = reshape(FFT.czt(array(x)), rows = x.size, columns = 1)
    var y = FFT.icztMatrix( xhat * psihat )
    y = transpose(y)
    y = Array(y.size) {
        y[it].slice(npad, norig+npad)
    }
    val f = wc / (2*PI*s) * fs
    val t = arange(0f, y[0].size().toFloat()) / fs
    return Triple(y, f, t)
}

val nv = 16
val g = 3f
val b = 10f/3f
val freqLimits = arrayOf(4f, 50f)
val fs = 100f
val norig = RESAMPLED_DATA_LENGTH
val s = getScales(norig, 0f, 0f, nv, g, b, freqLimits, fs).first
val w = getOmega(norig*2)
val wc = getMorseCenterFreq(g, b)
val psihat = morse(s, w, wc, g, b, 0f)
val f = wc / (2*PI*s) * fs
var t = arange(0f, RESAMPLED_DATA_LENGTH.toFloat()) / fs

fun cwt(x_: Array<Float>): Array<ComplexArray> {
    var x = x_.reversedArray() - mean(x_)
    var npad: Int = 0
    npad = norig/2
    val start = x.slice(0, npad,1).reversedArray()
    val end = x.slice(npad, x.size, 1).reversedArray()
    x = arrayOf(*start,  *x,  *end)
    val xhat = reshape(FFT.czt(array(x)), rows = x.size, columns = 1)
    var y = transpose( FFT.icztMatrix( xhat * psihat ) )
    y = Array(y.size) {
        y[it].slice(npad, norig+npad)
    }
    return y
}