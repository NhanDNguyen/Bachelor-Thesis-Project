package com.example.thesis3.dsp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val LOG2 = 0.3010299956639812
val POWER_OF_2 = listOf<Int>(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096)

fun getPowerOf2Length(L: Int) = POWER_OF_2[ceil(log10(L.toDouble()) / LOG2).toInt()]

fun zeropad(x: Array<Float>): Array<Float> {
    val length = POWER_OF_2[ceil(log10(x.size.toDouble()) / LOG2).toInt()] - x.size
    return  arrayOf(*x,  *Array<Float>(length) {0f})
}

fun zeropad(x: ComplexArray): ComplexArray {
    val length = POWER_OF_2[ceil(log10(x.real.size.toDouble()) / LOG2).toInt()] - x.real.size
    return array(
        re = arrayOf(*x.real, *Array<Float>(length) {0f}),
        img = arrayOf(*x.img, *Array<Float>(length) {0f})
    )
}

fun zeropad(x: Array<Float>, l: Int): Array<Float> {
    return arrayOf(*x, *Array<Float>(l) {0f})
}

fun zeropad(x: ComplexArray, l: Int): ComplexArray {
    return array(
        re = arrayOf(*x.real, *Array<Float>(l) {0f}),
        img = arrayOf(*x.img,  *Array<Float>(l) {0f})
    )
}

fun pad(x: Array<Float>): Array<Float> {
    val N = x.size
    val x_pad: Array<Float> = Array(x.size + N*2) { i ->
        x[i%x.size]
    }
    return x_pad
}

fun median(x: Array<Float>): Float {
    val m = x.sortedArray()
    if (m.size % 2 != 0) {
        return m[m.size / 2]
    }
    return (m[m.size/2] + m[m.size/2-1])/2f
}

fun zerofill(x: Array<Float>, L: Int): Array<Float> {
    val N = x.size
    var xIndex = -1
    val y: Array<Float> = Array(N * L) {
        if (it % L == 0) {
            x[++xIndex]
        } else {
            0f
        }
    }
    return y
}

fun zerofill(x: ComplexArray, L: Int): ComplexArray {
    return ComplexArray(
        real = zerofill(x.real, L),
        img = zerofill(x.img, L)
    )
}

fun decimate(x: Array<Float>, L: Int): Array<Float> {
    if (L <= 1)
        return x
    return Array((x.size/ L.toFloat()).toInt()) {
        x[it*L+1]
    }
}

fun decimate(x: ComplexArray, L: Int): ComplexArray {
    return array(decimate(x.real, L), decimate(x.img, L))

}

fun convolve(x: Array<Float>, h: Array<Float>): Array<Float> {
    val xSize = x.size
    val hSize = h.size
    val y = Array(xSize + hSize - 1) {0f}
    for (n in 0 until y.size-1) {
        val lwb = max(0, n-xSize+1)
        val upb = min(n, hSize-1)
        for (k in lwb until upb) {
            y[n] = y[n] + h[k] * x[n-k]
        }
    }
    return y
}

fun convolve(x: ComplexArray, h: ComplexArray): ComplexArray {
    val xSize = x.size()
    val hSize = h.size()
    val y = array(Array(xSize + hSize - 1) {0f})
    for (n in 0 until y.size()-1) {
        val lwb = max(0, n-xSize+1)
        val upb = min(n, hSize-1)
        for (k in lwb until upb) {
            y[n] = y[n] + h[k] * x[n-k]
        }
    }
    return y
}

fun fftconvolve(x: Array<Float>, h: Array<Float>, mode: String = "full"): Array<Float> {
    val l = x.size + h.size - 1
    //val l2 = getPowerOf2Length(l)
    val x_pad = array(zeropad(x, l - x.size))
    val h_pad = array(zeropad(h, l - h.size))
    if (mode == "same")
        return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) ).real.sliceArray(3 until 3+ x.size)
    return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) ).real
    return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) ).real.sliceArray(0 until l)
}

fun fftconvolve(x: ComplexArray, h: ComplexArray, mode: String = "full"): ComplexArray {
    val l = x.size() + h.size() - 1
    //val l2 = getPowerOf2Length(l)
    val x_pad = zeropad(x, l - x.size())
    val h_pad = zeropad(h, l - h.size())
    if (mode == "same")
        return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) ).slice(3, 3+ x.size())
    return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) )
    return FFT.iczt( FFT.czt(x_pad) * FFT.czt(h_pad) ).slice(0, l)
}

operator fun Array<Float>.minus(n: Float) = Array(this.size) {
    this[it] - n
}

fun Array<Float>.median(n: Float) = Array<Float> (this.size) {
    this[it].pow(n)
}

/*fun Array<Float>.ln() = Array<Float> (this.size) {
    ln( abs(this[it]) )
}*/

fun mean(s: Array<Float>) = s.sum() / s.size

fun std(s: Array<Float>): Float {
    val u = mean(s)
    return sqrt( (s - u).pow(2).sum() / (s.size-1) )
}

fun std(s: Array<Float>, u: Float): Float {
    return sqrt( (s - u).pow(2).sum() / (s.size) )
}

fun standardization(s: Array<Float>): Array<Float> {
    val u = mean(s)
    val std = std(s, u)
    return (s - u) / std
}

fun standardization(s: Array<Float>, u: Float): Array<Float> {
    return (s - u) / std(s, u)
}

fun cov(x: Array<Float>, y: Array<Float>): Float {
    return ( (x - mean(x))*(y - mean(y) ) ).sum() / (x.size - 1)
}

fun cov(x: Array<Float>, ux: Float, y: Array<Float>, uy: Float): Float {
    return ( (x - ux)*(y - uy) ).sum() / (x.size - 1)
}

fun eigenValues(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float): Float {
    var phi = 0f
    var eig1 = 0f
    var eig2 = 0f
    var eig3 = 0f
    val p1 = b.pow(2) + c.pow(2) + e.pow(2)
    if (p1 == 0f) {
        eig1 = a
        eig2 = d
        eig3 = f
    } else {
        val q = (a+d+f) / 3f
        val p = sqrt (((a-q).pow(2) + (d-q).pow(2) + (f-q).pow(2) + 2*p1) / 6f )
        val r = (1f/p.pow(3)) * ( (a-q)*((d-q)*(f-q)-e.pow(2)) + b*(c*e-b*(f-q)) + c*(b*e-(d-q)*c) ) / 2f
        if (r <= -1f)
            phi = PI / 3f
        else if (r >= 1f)
            phi = 0f
        else
            phi = acos(r) / 3f

        eig1 = q + 2 * p * cos(phi)
        //eig3 = q + 2 * p * cos(phi + (2f*PI/3f))
        //eig2 = 3 * q - eig1 - eig3
    }
    return eig1
}

fun PCA(x: Array<Float>, y: Array<Float>, z: Array<Float>): Array<Float> {
    var ux = 0f
    var uy = 0f
    var uz = 0f
    var sx: Array<Float> = Array(x.size) {0f}
    var sy: Array<Float> = Array(x.size) {0f}
    var sz: Array<Float> = Array(x.size) {0f}
    var a = 0f
    var d = 0f
    var f = 0f
    runBlocking(Dispatchers.Default) {
        launch {
            ux = mean(x)
            sx = standardization(x, ux)
            a = cov(x, ux, x, ux)
        }
        launch {
            uy = mean(y)
            sy = standardization(y, uy)
            d = cov(y, uy, y, uy)
        }
        launch {
            uz = mean(z)
            sz = standardization(z, uz)
            f = cov(z, uz, z, uz)
        }
    }
    val b = cov(x, ux, y, uy)
    val c = cov(x, ux, z, uz)
    val e = cov(y, uy, z, uz)

    val eigs = eigenValues(a, b, c, d, e, f)
    val m = a - eigs
    val k = f - eigs
    var z1 = 1f
    var y1 = -z1*(c*c-m*k) / (b*c-m*e)
    var x1 = (-e*y1 - k*z1) / c
    val qs = sqrt( z1.pow(2) + y1.pow(2) + x1.pow(2) )

    x1 /= qs
    y1 /= qs
    z1 /= qs

    val pc1 =  Array(x.size) {
        x1*sx[it] + y1*sy[it] + z1*sz[it]
    }
    /*pc1.forEachIndexed { it, value ->
        pc1[it] = value / maxV
    }*/
    //return pc1.sliceArray(4 until 50)
    return pc1
}

fun findLocalMin(arr: Array<Float>, n: Int): MutableList<Int> {
    val mn: MutableList<Int> = mutableListOf()
    for (i in 1 until n-1)
        if (arr[i] < arr[i-1] && arr[i] < arr[i+1])
            mn.add(i)
    return mn
}

fun argmin(S: Array<Float>) = S.indexOf(S.minOrNull())

fun moving_average(S: Array<Float>, ws: Int): Array<Float> {
    var n_minus_k = -1
    var i = 0
    var pi = 0f
    var limit = 0
    val A: MutableList<Float> = mutableListOf()
    while (i < S.size) {
        ++n_minus_k
        pi = 0f
        limit = n_minus_k + ws

        i = n_minus_k
        while ((i < limit) && (i < S.size)) {
            pi += S[i]
            ++i
        }
        A.add(pi/ws)
    }
    return A.toTypedArray()
}

fun beatSeperation(S: Array<Float>, U: Float, fps: Float, ws: Int=10): Array<Int> {
    val g = (60f * (fps/U)).toInt()
    val A = moving_average(S, ws)
    val minsS = findLocalMin(S, S.size)
    val minsA = findLocalMin(A, A.size)
    val B: MutableList<Int> = mutableListOf()
    minsA.forEach {i ->
        val i_start = max(0, i-g)
        var actual_offset = 0
        if (i-g >= 0)
            actual_offset = i - g
        val i_end = min(S.size, i+g)
        val Si = S.sliceArray(i_start until i_end)
        val j = argmin(Si) + actual_offset
        if (j in minsS)
            B.add(j)
    }
    return B.toTypedArray()
}

operator fun Float.plus(c: Complex): Complex{
    return Complex(this, 0f) + c
}

operator fun Float.minus(c: Complex): Complex{
    return Complex(this, 0f) - c
}

operator fun Float.times(c: Complex): Complex{
    return Complex(this* c.getReal(),this * c.getImg())
}

fun array(a: Array<Array<Float>>, type: String="real") = Array(a.size) {i ->
    array(a[i], type=type)
}

fun array(value: Array<Float>, type: String = "real"): ComplexArray {
    if ((type=="imaginary") || (type=="img")) {
        return ComplexArray(
            real = Array(value.size) {0f},
            img = value
        )
    }
    return ComplexArray(
        real = value,
        img = Array(value.size) {0f}
    )
}

operator fun Array<ComplexArray>.times(v: ComplexArray) = Array(this.size) {i ->
    this[i] * v
}

infix fun Float.divide(d: Float): Float {
    if (d == 0f) {
        return 0f
    }
    return this/d
}

infix fun Float.divide(d: Double): Float {
    if (d == 0.0) {
        return 0f
    }
    return (this/d).toFloat()
}

infix fun Float.divide(d: Int): Float {
    if (d == 0) {
        return 0f
    }
    return (this/d).toFloat()
}

fun Array<Float>.pow(v: Float): Array<Float> {
    return Array<Float>(this.size) { i ->
        this[i].pow(v)
    }
}

fun Array<Array<Float>>.pow(v: Float): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i].pow(v)
    }
}

fun Array<Float>.pow(v: Int): Array<Float> {
    return Array<Float>(this.size) { i ->
        this[i].pow(v)
    }
}

fun Array<Array<Float>>.pow(v: Int): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i].pow(v)
    }
}

operator fun Array<Float>.plus(v: Float): Array<Float> {
    return Array(this.size) { i ->
        this[i]+v
    }
}

operator fun Array<Float>.plus(v: Array<Float>): Array<Float> {
    if (this.size > v.size) {
        val b = zeropad(v, this.size - v.size)
        return Array(this.size) { i ->
            this[i]+b[i]
        }
    } else if (this.size < v.size) {
        val a = zeropad(this, v.size - this.size)
        return Array(this.size) { i ->
            this[i]+a[i]
        }

    }
    return Array(this.size) { i ->
        this[i]+v[i]
    }
}

operator fun Array<Array<Float>>.plus(v: Array<Array<Float>>): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i] + v[i]
    }
}

operator fun Array<Array<Float>>.plus(v: Float): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i] + v
    }
}

operator fun Array<Float>.minus(v: Array<Float>): Array<Float> {
    return Array(this.size) { i ->
        this[i] - v[i]
    }
}

operator fun Array<Array<Float>>.minus(v: Array<Array<Float>>): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i] - v[i]
    }
}

operator fun Float.times(array: Array<Float>): Array<Float> {
    return Array(array.size) { i ->
        array[i]*this
    }
}

operator fun Float.times(array: Array<Array<Float>>): Array<Array<Float>> {
    return Array(array.size) { i ->
        array[i]*this
    }
}

operator fun Array<Float>.times(v: Float): Array<Float> {
    return Array(this.size) { i ->
        this[i] * v
    }
}

operator fun Array<Float>.times(array: Array<Float>): Array<Float> {
    return Array(this.size) { i ->
        this[i]*array[i]
    }
}

operator fun Array<Array<Float>>.times(array: Array<Float>): Array<Array<Float>> {
    if (this[0].size==1) {
        return Array(this.size) { i ->
            this[i][0]*array
        }
    }
    return Array(this.size) { i ->
        this[i]*array
    }
}

operator fun Array<Array<Float>>.times(matrix: Array<Array<Float>>): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i]*matrix[i]
    }
}

operator fun ComplexArray.times(array: Array<Float>): ComplexArray {
    return array(
        re = this.real * array,
        img = this.img * array
    )
}

operator fun Array<ComplexArray>.times(array: Array<Float>): Array<ComplexArray> {
    return Array(this.size) { i ->
        this[i]*array
    }
}

operator fun Array<Float>.div(v: Float): Array<Float> {
    return Array(this.size) { i ->
        this[i].divide(v)
    }
}

operator fun Array<Array<Float>>.div(v: Float): Array<Array<Float>> {
    return Array(this.size) { i ->
        this[i] / v
    }
}

fun log10(array: Array<Float>): Array<Float> {
    return Array(array.size) { i ->
        log10(array[i])
    }
}

fun log10(matrix: Array<Array<Float>>): Array<Array<Float>> {
    return Array(matrix.size) { i ->
        log10(matrix[i])
    }
}

fun ln(matrix: Array<Array<Float>>): Array<Array<Float>> {
    return Array(matrix.size) { i ->
        ln(matrix[i])
    }
}

fun exp(array: Array<Float>): Array<Float> {
    return Array(array.size) { i ->
        exp(array[i])
    }
}

fun exp(matrix: Array<Array<Float>>): Array<Array<Float>> {
    return Array(matrix.size) { i ->
        exp(matrix[i])
    }
}

fun arrayOfSpecificNumber(rows: Int, value: Float=0f) = Array(rows) {value}

fun arrayOfSpecificNumber(rows: Int, columns: Int, value: Float=0f) = Array(rows) { Array(columns) {value} }

fun linspace(start: Float = 0f,
             stop: Float = 1f,
             N: Int = 50,
             endpoint: Boolean = true): Array<Float> {
    val increment = if (endpoint) (stop-start)/(N-1) else (stop-start)/N
    val a = Array<Float>(N) {i ->
        start + i*increment
    }
    return a
}

fun interpolateLinear(x: Array<Float>, xp: Array<Float>, yp: Array<Float>): Array<Float> {
    var k = 0
    var x0 = xp[k]
    var y0 = yp[k]
    var x1 = xp[k+1]
    var y1 = yp[k+1]
    val y: Array<Float> = Array<Float>(x.size) {i ->
        if (x[i] > x1) {
            ++k
            if ((k < yp.size) && (k+1 < yp.size)) {
                x0 = xp[k]
                y0 = yp[k]
                x1 = xp[k+1]
                y1 = yp[k+1]
            }
        }
        y0 + (x[i] - x0)*((y1-y0)/(x1-x0))
    }
    return  y
}

fun reshape(a: Array<Float>, rows: Int, columns: Int): Array<Array<Float>> {
    val array = Array(rows) { i ->
        Array(columns) { j ->
            a[i*columns+j]
        }
    }
    return array
}

fun reshape(a: Array<Array<Float>>, rows: Int, columns: Int): Array<Array<Float>> {
    val colsOriginal = a[0].size
    var k = 0
    var m = -1
    val array = Array(rows) { i ->
        Array(columns) { j ->
            ++m
            if (m == colsOriginal) {
                k += 1
                m = 0
            }
            a[k][m]
        }
    }
    return array
}

fun reshape(a: Array<Array<Float>>) = a.reduce{ acc, floatArray -> acc + floatArray }

fun reshape(a: ComplexArray, rows: Int, columns: Int): Array<ComplexArray> {
    val array = Array(rows) { i ->
        val re = Array(columns) { j ->
            a.real[i*columns+j]
        }
        val img = Array(columns) { j ->
            a.img[i*columns+j]
        }
        array(re, img)
    }
    return array
}

fun reshape(a: Array<ComplexArray>, rows: Int, columns: Int): Array<ComplexArray> {
    val colsOriginal = a[0].size()
    var k = 0
    var m = -1
    val array = Array(rows) { i ->
        val re = Array(columns) {0f}
        val img = Array(columns) {0f}
        for (j in 0 until columns) {
            ++m
            if (m == colsOriginal) {
                k += 1
                m = 0
            }
            re[j] = a[k].real[m]
            img[j] = a[k].img[m]
        }
        array(re, img)
    }
    return array
}

fun reshape(a: Array<ComplexArray>) = a.reduce{ acc, complexArray -> concatAndReturn(acc, complexArray) }

fun transpose(matrix: Array<Array<Float>>) = Array(matrix[0].size) {i ->
    Array(matrix.size) {j ->
        matrix[j][i]
    }
}

fun transpose(matrix: Array<ComplexArray>) = Array(matrix[0].size()) {i ->
    val re = Array(matrix.size) {j ->
        matrix[j].real[i]
    }
    val img = Array(matrix.size) {j ->
        matrix[j].img[i]
    }
    array(re, img)
}

fun gammaln(num: Float): Double {
    return ln( abs(_gammaLanczos(num.toDouble())) )
}

fun gammaln(num: Double): Double {
    return ln( abs(_gammaLanczos(num)) )
}

fun gamma(num: Float): Double {
    val a = _gammaLanczos(num.toDouble())
    return a
}

fun _gammaLanczos(x: Double): Double {
    var xx = x
    val p = doubleArrayOf(
        0.99999999999980993,
        676.5203681218851,
        -1259.1392167224028,
        771.32342877765313,
        -176.61502916214059,
        12.507343278686905,
        -0.13857109526572012,
        9.9843695780195716e-6,
        1.5056327351493116e-7
    )
    val g = 7
    if (xx < 0.5) return Math.PI / (Math.sin(Math.PI * xx) * _gammaLanczos(1.0 - xx))
    xx--
    var a = p[0]
    val t = xx + g + 0.5
    for (i in 1 until p.size) a += p[i] / (xx + i)
    return Math.sqrt(2.0 * Math.PI) * Math.pow(t, xx + 0.5) * Math.exp(-t) * a
}

fun laguerre(X: Array<Float>, k: Float, c: Float): Array<Float> {
    var sn = 1
    var Y = sn * abs( (gamma(k+c+1)/gamma(k+1)) / (gamma(c+1)*gamma(1f)) ).toFloat() * X.pow(0f)
    for (m in 1 until (k+1).toInt()) {
        sn = -sn
        Y += sn * abs((gamma(k + c + 1) / gamma(k - m + 1)) / (gamma(c + m + 1) * gamma(m + 1f))).toFloat() * X.pow(m)
    }
    return Y
}

fun laguerre(X: Array<Array<Float>>, k: Float, c: Float): Array<Array<Float>> {
    var Y = abs( (gamma(k+c+1)/gamma(k+1)) / (gamma(c+1)*gamma(1f)) ).toFloat() * X.pow(0f)
    for (m in 1 until (k+1).toInt()) {
        Y += (-1f)*abs((gamma(k + c + 1) / gamma(k - m + 1)) / (gamma(c + m + 1) * gamma(m + 1f))).toFloat() * X.pow(m)
    }
    return Y
}

fun ComplexArray.slice(start: Int, end: Int, step: Int=0): ComplexArray {
    return array(
        re = real.slice(start, end, step),
        img = img.slice(start, end, step)
    )
}

fun Array<Float>.slice(start: Int, end: Int, step: Int=0): Array<Float> {
    var index = start
    var endIndex = end
    if (start == -1) {
        index = this.size-1
    }
    if (end == -1) {
        endIndex = this.size-1
    }
    if (step != 0) {
        return Array(abs((index-endIndex))/(abs(step))) {i ->
            this[index+step*i]
        }
    }
    return this.sliceArray(start until end)
}

fun Array<Array<Float>>.slice(start: Int, end: Int, step: Int=0): Array<Array<Float>> {
    var index = start
    var endIndex = end
    if (start == -1) {
        index = this.size-1
    }
    if (end == -1) {
        endIndex = this.size-1
    }
    if (step != 0) {
        return Array(abs((index-endIndex-1))/(abs(step))) {i ->
            this[index+step*i]
        }
    }
    return this.sliceArray(index until endIndex)
}

fun Array<ComplexArray>.slice(start: Int, end: Int, step: Int=0): Array<ComplexArray> {
    var index = start
    var endIndex = end
    if (start == -1) {
        index = this.size-1
    }
    if (end == -1) {
        endIndex = this.size-1
    }
    if (step != 0) {
        return Array(abs((index-endIndex)/step)) {i ->
            this[index+step*i]
        }
    }
    return this.sliceArray(index until  endIndex)
}

fun arange(start: Int = 0, stop: Int, step: Int = 1): Array<Float> {
    if (start > stop) {
        return Array(ceil((start - stop) / step.toDouble()).toInt()) {
            //(it + start) * step
            (start - step*it).toFloat()
        }
    }
    return Array(ceil((stop - start) / step.toDouble()).toInt()) {
        //(it + start) * step
        (start + step*it).toFloat()
    }
}

fun arange(start: Float = 0f, stop: Float, step: Float = 1f): Array<Float> {
    if (start > stop) {
        return Array(ceil((start - stop) / step.toDouble()).toInt()) {
            //(it + start) * step
            (start - step*it)
        }
    }
    return Array(ceil((stop - start) / step.toDouble()).toInt()) {
        //(it + start) * step
        (start + step*it)
    }
}

fun<Object> reassignArray(
    a: Array<Array<Object>>,
    position: Triple<Int, Int, Int>,
    positionIndices: List<Int>,
    replacement: Triple<Int, Int, Int>,
    replacementIndices: List<Int>
) {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = a.size-1
    }
    if (position.second == -1) {
        endIndex = a.size-1
    }
    var indexReplacement = replacement.first
    if (replacement.first == -1) {
        indexReplacement = a.size-1
    }
    for (i in 0 until abs((index-endIndex)/position.third)/2) {
        val rowIndex = index+position.third*i
        for (j in 0 until positionIndices.size) {
            swap(a, Pair(rowIndex, positionIndices[j]), Pair(indexReplacement, replacementIndices[j]))
        }
        indexReplacement += replacement.third
    }
}

fun reassignArray(
    a: Array<ComplexArray>,
    position: Triple<Int, Int, Int>,
    positionIndices: List<Int>,
    replacement: Triple<Int, Int, Int>,
    replacementIndices: List<Int>
) {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = a.size-1
    }
    if (position.second == -1) {
        endIndex = a.size-1
    }
    var indexReplacement = replacement.first
    if (replacement.first == -1) {
        indexReplacement = a.size-1
    }
    for (i in 0 until abs((index-endIndex)/position.third)/2) {
        val rowIndex = index+position.third*i
        for (j in 0 until positionIndices.size) {
            swap(a, Pair(rowIndex, positionIndices[j]), Pair(indexReplacement, replacementIndices[j]))
        }
        indexReplacement += replacement.third
    }
}

fun swap(array: Array<ComplexArray>, p1: Pair<Int, Int>, p2: Pair<Int, Int>) {
    val temp = array[p1.first][p1.second]
    array[p1.first][p1.second] = array[p2.first][p2.second]
    array[p2.first][p2.second] = temp
}

fun <Object>swap(array: Array<Array<Object>>, p1: Pair<Int, Int>, p2: Pair<Int, Int>) {
    val temp: Object = array[p1.first][p1.second]
    array[p1.first][p1.second] = array[p2.first][p2.second]
    array[p2.first][p2.second] = temp
}

fun Array<Array<Float>>.getValues(position: Triple<Int, Int, Int>, indices: List<Int>): Array<Array<Float>> {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = this.size-1
    }
    if (position.second == -1) {
        endIndex = this.size-1
    }
    if (position.third != 0) {
        return Array(abs((index-endIndex)/position.third)) {i ->
            val currentIndex = index+position.third*i
            Array(indices.size) { j ->
                this[currentIndex][indices[j]]
            }
        }
    }
    return Array(abs(index-endIndex)) {i ->
        val currentIndex = index*i
        Array(indices.size) { j ->
            this[currentIndex][indices[j]]
        }
    }
}

fun Array<Float>.getValues(position: Triple<Int, Int, Int>, indices: List<Int>): Array<Float> {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = this.size-1
    }
    if (position.second == -1) {
        endIndex = this.size-1
    }
    if (position.third != 0) {
        return Array(indices.size) {i ->
            this[indices[i]]
        }
    }
    return Array(indices.size) {i ->
        this[indices[i]]
    }
}

fun Array<Float>.compareTo(v: Float, e: String): List<Int> {
    return when (e) {
        ">" -> this.withIndex().filter { it.value > v }.map { it.index }
        ">=" -> this.withIndex().filter { it.value >= v }.map { it.index }
        "<" -> this.withIndex().filter { it.value < v }.map { it.index }
        "<=" -> this.withIndex().filter { it.value <= v }.map { it.index }
        "==" -> this.withIndex().filter { it.value == v }.map { it.index }
        "!=" -> this.withIndex().filter { it.value != v }.map { it.index }
        else -> listOf()
    }
}

fun reassignArray(
    a: Array<ComplexArray>,
    position: Triple<Int, Int, Int>,
    positionIndices: List<Int>,
    replacementValues: Array<ComplexArray>
) {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = a.size-1
    }
    if (position.second == -1) {
        endIndex = a.size-1
    }
    for (i in 0 until abs((index-endIndex)/position.third)) {
        val rowIndex = index+position.third*i
        for (j in 0 until positionIndices.size) {
            a[rowIndex][positionIndices[j]] = replacementValues[i][j]
        }
    }
}

fun Array<ComplexArray>.getValues(position: Triple<Int, Int, Int>, indices: List<Int>): Array<ComplexArray> {
    var index = position.first
    var endIndex = position.second
    if (position.first == -1) {
        index = this.size-1
    }
    if (position.second == -1) {
        endIndex = this.size-1
    }
    if (position.third != 0) {
        return Array(abs((index-endIndex)/position.third)) {i ->
            val currentIndex = index+position.third*i
            array(
                re = Array(indices.size) { j ->
                    this[currentIndex].real[indices[j]]
                },
                img = Array(indices.size) { j ->
                    this[currentIndex].img[indices[j]]
                }
            )
        }
    }
    return Array(abs(index-endIndex)) {i ->
        val currentIndex = index*i
        array(
            re = Array(indices.size) { j ->
                this[currentIndex].real[indices[j]]
            },
            img = Array(indices.size) { j ->
                this[currentIndex].img[indices[j]]
            }
        )
    }
}

fun Array<ComplexArray>.conjugate() {
    this.forEach {
        it.conjugate()
    }
}

fun ComplexArray.conjugate() {
    for (i in 0 until this.size()) {
        this.img[i] *= -1f
    }
}

fun Array<ComplexArray>.conjugateAndReturn() = Array(this.size) {i ->
    this[i].conjugateAndReturn()
}


fun Complex.conjugate() {
    this.im *= -1f
}

fun ComplexArray.conjugateAndReturn() = array(
    re = this.real,
    img = this.img*-1f
)

fun sqrtComplex(v: Float): Complex {
    if (v < 0f) {
        return Complex(0f, sqrt(abs(v)))
    }
    return Complex(sqrt(v), 0f)
}

fun ln(array: Array<Float>) = Array(array.size) {
    ln(array[it])
}

fun Float.pow(array: Array<Float>) = Array(array.size) {
    this.pow(array[it])
}

operator fun Float.div(array: Array<Float>) = Array(array.size) {
    this/array[it]
}

operator fun Array<ComplexArray>.div(v: Float) = Array(this.size) {
    this[it]/v
}

operator fun Array<ComplexArray>.div(v: Int) = Array(this.size) {
    this[it]/v
}

operator fun Array<ComplexArray>.times(v: Float) = Array(this.size) {i ->
    this[i]*v
}

operator fun ComplexArray.div(v: Int) = array(
    re = this.real/v,
    img = this.img/v
)

operator fun Array<Float>.div(v: Int) = Array(this.size) {
    this[it] / v
}

fun array(matrix: Array<ComplexArray>) = matrix.copyOf()

fun Complex.pow(array: Array<Float>): ComplexArray {
    val abs = this.abs()
    val angle = this.angle()
    return array(
        re = Array(array.size) {
            abs.pow(array[it]) * cos(angle*array[it])
        },
        img = Array(array.size) {
            abs.pow(array[it]) * sin(angle*array[it])
        }
    )
}

operator fun Float.div(complexArray: ComplexArray): ComplexArray {
    return array(
        re = Array(complexArray.size()) {
            this*complexArray[it].re / (complexArray[it].re.pow(2) + complexArray[it].im.pow(2))
        },
        img = Array(complexArray.size()) {
            -this*complexArray[it].im / (complexArray[it].re.pow(2) + complexArray[it].im.pow(2))
        }
    )
}

fun expComplex(x: Array<Float>): ComplexArray {
    return ComplexArray(
        real = Array(x.size) { cos(x[it]) },
        img = Array(x.size) { sin(x[it]) }
    )
}

operator fun Array<ComplexArray>.times(matrix: Array<ComplexArray>): Array<ComplexArray> {
    if (this[0].size()==1) {
        return Array(this.size) { i ->
            matrix[i] * this[i][0]
        }
    } else if (matrix[0].size()==1) {
        return Array(this.size) { i ->
            this[i]*matrix[i][0]
        }
    }
    return Array(this.size) { i ->
        this[i] * matrix[i]
    }
}













