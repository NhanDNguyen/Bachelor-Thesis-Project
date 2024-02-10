package com.example.thesis3.dsp

import kotlin.math.*

class Complex {
    var re: Float = 0f
    var im: Float = 0f

    constructor() {
        re = 0f
        im = 0f
    }

    constructor(re: Float, im: Float) {
        this.re = re
        this.im = im
    }

    constructor(radian: Float) {
        re = cos(radian)
        im = sin(radian)
    }

    infix operator fun plus(x: Complex) = Complex(re + x.re, im + x.im)
    infix operator fun plus(x: Float) = Complex(re + x, im)
    infix operator fun minus(x: Complex) = Complex(re - x.re, im - x.im)
    infix operator fun times(x: Float) = Complex(re * x, im * x)
    infix operator fun times(x: Complex) = Complex(re * x.re - im * x.im, re * x.im + im * x.re)
    infix operator fun div(x: Float) = Complex(re / x, im / x)
    infix operator fun div(x: Complex) = Complex(
        (this.re*x.re+this.im*x.im)/(x.re*x.re+x.im*x.im),
        (this.im*x.re-this.re*x.im)/(x.re*x.re+x.im*x.im) )

    fun Complex.abs() = sqrt(this.re.pow(2) + this.im.pow(2))

    fun Complex.angle(): Float {
        if (this.re >= 0f) {
            return atan(this.im/this.re)
        }
        return atan(this.im/this.re) + PI
    }

    fun pow(n: Int): Complex {
        val abs = this.abs()
        val angle = this.angle()
        return Complex(abs.pow(n)*cos(angle*n), abs.pow(n)*sin(angle*n))
    }

    fun pow(n: Float): Complex {
        val abs = this.abs()
        val angle = this.angle()
        return Complex(abs.pow(n)*cos(angle*n), abs.pow(n)* sin(angle*n))
    }
    val exp: Complex by lazy { Complex(cos(im), sin(im)) * (cosh(re) + sinh(re)) }

    override fun toString(): String {
        if (im < 0) {
            return "$re-${abs(im)}i"
        }
        return "$re+${abs(im)}i"
    }

    fun round(p: Int) {
        val pow = 10f.pow(p)
        re = (re * pow).roundToInt().toFloat() / pow
        im = (im * pow).roundToInt().toFloat() / pow
    }

    fun getReal(): Float {
        return re
    }

    fun getImg(): Float {
        return im
    }
}

val Float.j: Complex
    get() = Complex(0f, this)

fun expComplex(rad: Float): Complex {
    return Complex(rad)
}