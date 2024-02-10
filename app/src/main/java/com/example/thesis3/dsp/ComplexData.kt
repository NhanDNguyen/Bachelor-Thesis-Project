package com.example.thesis3.dsp

import kotlin.math.*

val PI = kotlin.math.PI.toFloat()
data class ComplexArray(
    var real: Array<Float> = emptyArray(),
    var img: Array<Float> = emptyArray(),
) {
    override fun toString(): String {
        if (real.size != img.size) {
            return "size of real and imaginary units is not match"
        }
        var s = "["
        real.forEachIndexed() { index, value ->
            if (img[index] < 0){
                s += "$value-${abs(img[index])}i\n"
            } else {
                s += "$value+${abs(img[index])}i\n"
            }
        }
        s += "]"
        return s
    }
}

fun array() = ComplexArray(
    real = emptyArray(),
    img = emptyArray()
)

fun array(re: Array<Float>) = ComplexArray(
    real = re,
    img = Array(re.size) {0f}
)

fun array(re: Array<Float>, img: Array<Float>): ComplexArray {
    if (re.size == img.size) {
        return ComplexArray(
            real = re.clone(),
            img = img.clone()
        )
    }
    return ComplexArray(
        real = emptyArray(),
        img = emptyArray()
    )
}

fun array(ComplexArray: ComplexArray): ComplexArray {
    return ComplexArray(
        real = ComplexArray.real.clone(),
        img = ComplexArray.img.clone()
    )
}

fun array(complexArray: Array<Complex>): ComplexArray {
    //val complexArray = ComplexArray.
    val re = Array<Float>(complexArray.size) {0f}
    val im = Array<Float>(complexArray.size) {0f}
    complexArray.forEachIndexed() { index, complex ->
        re[index] = complex.re
        im[index] = complex.im
    }
    return ComplexArray(
        real = re,
        img = im
    )
}

infix operator fun ComplexArray.plus(x: ComplexArray): ComplexArray {
    if (this.size() > x.size()) {
        val b = zeropad(x, this.size() - x.size())
        return array(
            Array(b.real.size) { b.real[it] + this.real[it] },
            Array(b.real.size) { b.img[it] + this.img[it] }
        )
    } else if (this.size() < x.size()) {
        val a = zeropad(this, x.size() - this.size())
        return array(
            Array(a.real.size) { a.real[it] + x.real[it] },
            Array(a.real.size) { a.img[it] + x.img[it] }
        )
    }
    return array(
        Array(x.real.size) { x.real[it] + this.real[it] },
        Array(x.real.size) { x.img[it] + this.img[it] }
    )
}

infix operator fun ComplexArray.plus(x: Float) = ComplexArray (
    real = Array(this.real.size) { this.real[it]+x },
    img = this.img
)

infix operator fun ComplexArray.plus(x: Complex): ComplexArray {
    if (x.im == 0f) {
        return ComplexArray(
            real = this.real,
            img = Array(this.img.size) { this.img[it] + x.im }
        )
    } else if (x.re == 0f) {
        return ComplexArray(
            real = Array(this.real.size) { this.real[it] + x.re },
            img = this.img
        )
    }
    return ComplexArray(
        real = Array(this.real.size) { this.real[it] + x.re },
        img = Array(this.img.size) { this.img[it] + x.im }
    )
}

infix operator fun ComplexArray.minus(x: ComplexArray) = ComplexArray(
    real = Array(x.real.size) { x.real[it] - this.real[it] },
    img = Array(x.img.size) { x.img[it] - this.img[it] }
)

infix operator fun ComplexArray.minus(x: Float) = ComplexArray (
    real = Array(this.real.size) { this.real[it]-x },
    img = this.img
)

infix operator fun ComplexArray.minus(x: Complex): ComplexArray {
    if (x.im == 0f) {
        return ComplexArray(
            real = this.real,
            img = Array(this.img.size) { this.img[it] - x.im }
        )
    } else if (x.re == 0f) {
        return ComplexArray(
            real = Array(this.real.size) { this.real[it] - x.re },
            img = this.img
        )
    }
    return ComplexArray(
        real = Array(this.real.size) { this.real[it] - x.re },
        img = Array(this.img.size) { this.img[it] - x.im }
    )
}

infix operator fun ComplexArray.times(x: ComplexArray): ComplexArray {
    if (this.size() == x.size()) {
        return array(
            re = Array(x.real.size) { x.real[it] * this.real[it] - x.img[it] * this.img[it] },
            img = Array(x.img.size) { x.real[it] * this.img[it] + x.img[it] * this.real[it] }
        )
    } else if (this.size() == 1) {
        return array(
            re = Array(x.real.size) { x.real[it] * this.real[0] - x.img[it] * this.img[0] },
            img = Array(x.img.size) { x.real[it] * this.img[0] + x.img[it] * this.real[0] }
        )
    } else {
        return array(
            re = Array(x.real.size) { x.real[0] * this.real[it] - x.img[0] * this.img[it] },
            img = Array(x.img.size) { x.real[0] * this.img[it] + x.img[0] * this.real[it] }
        )
    }
}

infix operator fun ComplexArray.times(x: Complex) = ComplexArray (
    real = Array(this.real.size) { x.re*this.real[it] - x.im*this.img[it] },
    img = Array(this.img.size) { x.re*this.img[it] + x.im*this.real[it] }
)

infix operator fun ComplexArray.times(x: Float) = ComplexArray (
    real = Array(this.real.size) { this.real[it]*x },
    img = Array(this.img.size) { this.img[it]*x }
)

infix operator fun ComplexArray.div(x: ComplexArray) = ComplexArray (
    real = Array(x.real.size) { (this.real[it]*x.real[it]+this.img[it]*x.img[it])/
            (x.real[it]*x.real[it]+x.img[it]*x.img[it]) },
    img = Array(x.img.size) { (this.img[it]*x.real[it]-this.real[it]*x.img[it])/
            (x.real[it]*x.real[it]+x.img[it]*x.img[it]) }
)

infix operator fun ComplexArray.div(x: Complex) = ComplexArray (
    real = Array(this.real.size) { (this.real[it]*x.re+this.img[it]*x.im)/
            (x.re*x.re+x.im*x.im) },
    img = Array(this.img.size) { (this.img[it]*x.re-this.real[it]*x.im)/
            (x.re*x.re+x.im*x.im) }
)

infix operator fun ComplexArray.div(x: Float) = ComplexArray (
    real = Array(this.real.size) { this.real[it]/x },
    img = Array(this.img.size) { this.img[it]/x }
)

fun concatAndReturn(x: ComplexArray, y: ComplexArray): ComplexArray {
    return array(
        re = arrayOf( *x.real, *y.real),
        img = arrayOf( *x.img, *y.img)
    )
}

infix operator fun ComplexArray.get(i: Int): Complex {
    return Complex(getRealAtIndex(i), getImgAtIndex(i))
}

fun concat(x: ComplexArray, y: ComplexArray) {
    x.real = arrayOf(*x.real, *y.real)
    x.img = arrayOf(*x.img, *y.img)
}

infix fun ComplexArray.getRealAtIndex(index: Int): Float {
    return this.real[index]
}

infix fun ComplexArray.getImgAtIndex(index: Int): Float {
    return this.img[index]
}

infix fun ComplexArray.getComplexAtIndex(index: Int): Complex {
    return Complex(this.real[index], this.img[index])
}

infix fun ComplexArray.getValueAtIndex(index: Int): Pair<Float, Float> {
    return Pair(this.real[index], this.img[index])
}

fun ComplexArray.getRealsAndImgs(): Pair<Array<Float>, Array<Float>> {
    return Pair(this.real, this.img)
}

fun ComplexArray.getReals(): Array<Float> {
    return real
}

fun ComplexArray.getImgs(): Array<Float> {
    return img
}

fun ComplexArray.abs(): Array<Float>{
    return Array(real.size) {
        sqrt(real[it]*real[it] + img[it]*img[it])
    }
}

fun ComplexArray.angle(): Array<Float> {
    return Array(real.size) {
        if (real[it] >= 0f) {
            atan(img[it]/real[it])
        } else {
            atan(img[it]/real[it]) + PI
        }
    }
}

fun ComplexArray.toExp(): ComplexArray {
    return ComplexArray(
        Array(real.size) { cos(img[it]) * (cosh(real[it]) + sinh(real[it])) },
        Array(img.size) { sin(img[it]) * (cosh(real[it]) + sinh(real[it])) }
    )
}

fun exp(x: Float, L: Int): ComplexArray {
    return ComplexArray(
        real = Array(L) { cos(x*it) },
        img = Array(L) { sin(x*it) }
    )
}

fun exp(x: ComplexArray): ComplexArray {
    return ComplexArray(
        real = Array(x.real.size) { cos(x.real[it]) },
        img = Array(x.img.size) { sin(x.real[it]) }
    )
}

operator fun Float.times(x: ComplexArray): ComplexArray {
    return x*this
}

fun ComplexArray.slice(start: Int, end: Int): ComplexArray {
    return array(
        re = real.sliceArray(start until end),
        img = img.sliceArray(start until end)
    )
}

fun reduceFraction(x: Int, y: Int): Pair<Int, Int> {
    fun gcd(a: Int, b: Int): Int {
        if (b == 0) {
            return a
        }
        return gcd(b, a % b)
    }
    val d = gcd(x, y)
    return Pair(x/d, y/d)
}

fun ComplexArray.size() = real.size

operator fun ComplexArray.set(n: Int, value: Complex) {
    real[n] = value.re
    img[n] = value.im
}
