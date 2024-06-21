package dev.runefox.matlin

import kotlin.math.sqrt

const val beforeStart = -1.0
const val afterEnd = -2.0

private fun Bezier.sampleLinearLength(len: Double): Double {
    val dx = ex - sx
    val dy = ey - sy

    val selfLen = sqrt(dx * dx + dy * dy)
    if (selfLen == 0.0)
        return Double.NaN

    val t = len / selfLen
    if (t > 1)
        return -selfLen
    if (t < 0)
        return Double.NaN
    return t
}

fun Linear.sampleLength(len: Double): Double {
    val sample = sampleLinearLength(len)
    if (sample.isNaN())
        return beforeStart
    if (sample < 0)
        return afterEnd
    return sample
}

fun Quadratic.sampleLength0(len: Double, nonlinearityThreshold: Double = defThreshold): Double {
    // Test if curve is close enough to a line
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        return sampleLinearLength(len)
    }

    val (sx, sy, ax, ay, ex, ey) = this

    cutStart(0.5)
    val l1 = sampleLength0(len, nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    if (l1.isNaN())
        return l1
    if (l1 >= 0)
        return l1 * 0.5

    cutEnd(0.5)
    val l2 = sampleLength0(len + l1, nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    if (l2.isNaN())
        return l2
    if (l2 >= 0)
        return l2 * 0.5 + 0.5

    return l1 + l2
}

fun Quadratic.sampleLength(len: Double, nonlinearityThreshold: Double = defThreshold): Double {
    val sample = sampleLength0(len, nonlinearityThreshold)
    if (sample.isNaN())
        return beforeStart
    if (sample < 0)
        return afterEnd
    return sample
}

fun Cubic.sampleLength0(len: Double, nonlinearityThreshold: Double = defThreshold): Double {
    // Test if curve is close enough to a line
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        return sampleLinearLength(len)
    }

    val (sx, sy, ax, ay, bx, by, ex, ey) = this

    cutStart(0.5)
    val l1 = sampleLength0(len, nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    if (l1.isNaN())
        return l1
    if (l1 >= 0)
        return l1 * 0.5

    cutEnd(0.5)
    val l2 = sampleLength0(len + l1, nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    if (l2.isNaN())
        return l2
    if (l2 >= 0)
        return l2 * 0.5 + 0.5

    return l1 + l2
}

fun Cubic.sampleLength(len: Double, nonlinearityThreshold: Double = defThreshold): Double {
    val sample = sampleLength0(len, nonlinearityThreshold)
    if (sample.isNaN())
        return beforeStart
    if (sample < 0)
        return afterEnd
    return sample
}

fun Bezier.sampleLength(len: Double, nonlinearityThreshold: Double = defThreshold): Double {
    return when (this) {
        is Linear -> sampleLength(len, )
        is Quadratic -> sampleLength(len, nonlinearityThreshold)
        is Cubic -> sampleLength(len, nonlinearityThreshold)
    }
}
