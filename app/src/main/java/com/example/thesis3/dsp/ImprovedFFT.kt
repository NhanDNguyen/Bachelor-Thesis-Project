package com.example.thesis3.dsp

import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

/********************** Fast Fourier Transform function **********************/
object FFT {
    fun fft(a: ComplexArray) = _fft(a)
    fun ifft(a: ComplexArray): ComplexArray {
        return _fft(a, invert = 1) / a.size()
    }

    fun  czt(x: ComplexArray) = _czt(x)
    fun  iczt(x: ComplexArray) = _czt(x, invert = 1) / x.size()

    fun fftMatrix(matrix: Array<ComplexArray>) = _fftMatrix(matrix)
    fun ifftMatrix(matrix: Array<ComplexArray>) = _fftMatrix(matrix, invert = 1) / matrix.size

    fun cztMatrix(matrix: Array<ComplexArray>) = _cztMatrix(matrix)
    fun icztMatrix(matrix: Array<ComplexArray>) = _cztMatrix(matrix, invert = 1) / matrix.size

    private fun _czt(x: ComplexArray, invert: Int = - 1): ComplexArray {
        val n = x.size()
        val m = n
        val w: Complex = expComplex((2*invert* kotlin.math.PI /m).toFloat())
        val a = 1

        val chirp = w.pow(arange(1-n, max(m, n)).pow(2)/2f )
        val N2: Int = 2f.pow(ceil(log2((m+n-1).toFloat()))).toInt()
        val xp = concatAndReturn(x*chirp.slice(n-1, n+n-1), array(arrayOfSpecificNumber(N2-n, value = 0f)))
        val ichirpp = concatAndReturn( 1f/chirp.slice(0, m+n-1), array(arrayOfSpecificNumber(N2-(m+n-1), value = 0f)) )
        val r = ifft(fft(xp) * fft(ichirpp))
        return r.slice(n-1, m+n-1) * chirp.slice(n-1, m+n-1)
    }

    private fun _cztMatrix(x: Array<ComplexArray>, invert: Int = - 1): Array<ComplexArray> {
        val n = x.size
        val m = n
        val w: Complex = expComplex((2*invert* kotlin.math.PI /m).toFloat())
        val a = 1

        val chirp = w.pow(arange(1-n, max(m, n)).pow(2)/2f )
        val N2: Int = 2f.pow(ceil(log2((m+n-1).toFloat()))).toInt()
        val xp_chirp = chirp.slice(n-1, n+n-1)
        val xp: Array<ComplexArray> = array(arrayOfSpecificNumber(rows = x.size+(N2-n), columns = x[0].size(), value = 0f))
        for (i in 0 until xp[0].size()) {
            for (j in 0 until xp_chirp.size()) {
                xp[j][i] = x[j][i] * xp_chirp[j]
            }
        }
        val ichirpp = 1f/chirp.slice(0, m+n-1)
        val ichirpp_matrix = array(arrayOfSpecificNumber(rows = xp.size, columns = xp[0].size(), value = 0f))
        for (i in 0 until ichirpp_matrix[0].size()) {
            for (j in 0 until ichirpp.size()) {
                ichirpp_matrix[j][i] = ichirpp[j]
            }
        }

        val r = ifftMatrix(fftMatrix(xp) * fftMatrix(ichirpp_matrix))
        return r.slice(n-1, m+n-1) * reshape(chirp.slice(n-1, m+n-1), rows = m, columns = 1)
    }

    private fun swap(a: ComplexArray, i: Int, j: Int) {
        val tmp = a[i]
        a[i] = a[j]
        a[j] = tmp
    }

    private fun _fftMatrix(a: Array<ComplexArray>, invert: Int = -1): Array<ComplexArray> {
        val y = array(a)
        val n: Int = a.size
        val cols: Int = a[0].size()
        var j: Int = 0
        var len: Int = 2

        for (i in 1 until n) {
            var bit: Int = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                for (colIndex in 0 until cols) {
                    val tmp = y[i][colIndex]
                    y[i][colIndex] = y[j][colIndex]
                    y[j][colIndex] = tmp
                }
            }
        }

        while (len <= n) {
            val wlen: Complex = Complex((2* kotlin.math.PI /len * invert).toFloat())
            for (i in 0 until n step len) {
                var w = Complex(1f, 0f)
                j = 0
                while (j < len/2) {
                    for (colIndex in 0 until cols) {
                        val u = y[i + j][colIndex]; val v = y[i+j+len/2][colIndex] * w
                        y[i+j][colIndex] = u + v
                        y[i+j+len/2][colIndex] = u - v
                    }
                    w *= wlen
                    j++
                }
            }
            len = len shl 1
        }
        return y
    }

    private fun _fft(a: ComplexArray, invert: Int = -1): ComplexArray {
        val y = array(a)
        val n: Int = a.size()
        var j: Int = 0
        var len: Int = 2

        for (i in 1 until n) {
            var bit: Int = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j)
                swap(y, i, j)
        }

        while (len <= n) {
            val wlen: Complex = Complex(2* PI /len * invert)
            for (i in 0 until n step len) {
                var w = Complex(1f, 0f)
                j = 0
                while (j < len/2) {
                    val u = y[i + j]; val v = y[i+j+len/2] * w
                    y[i+j] = u + v
                    y[i+j+len/2] = u - v
                    w *= wlen
                    j++
                }
            }
            len = len shl 1
        }
        return y
    }
}
/********************** Fast Fourier Transform function **********************/