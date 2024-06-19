package dev.runefox.matlin

import kotlin.math.sqrt

const val defThreshold = 0.000001

private fun Bezier.linearLength(): Double {
    val dx = ex - sx
    val dy = ey - sy

    return sqrt(dx * dx + dy * dy)
}

fun Linear.length(): Double {
    return linearLength()
}

fun Quadratic.length(nonlinearityThreshold: Double = defThreshold): Double {
    // Test if curve is close enough to a line
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        return linearLength()
    }

    val (sx, sy, ax, ay, ex, ey) = this

    cutStart(0.5)
    val l1 = length(nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    cutEnd(0.5)
    val l2 = length(nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    return l1 + l2
}

fun Cubic.length(nonlinearityThreshold: Double = defThreshold): Double {
    // Test if curve is close enough to a line
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        return linearLength()
    }

    val (sx, sy, ax, ay, bx, by, ex, ey) = this

    cutStart(0.5)
    val l1 = length(nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    cutEnd(0.5)
    val l2 = length(nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    return l1 + l2
}

fun Bezier.length(nonlinearityThreshold: Double = defThreshold): Double {
    return when (this) {
        is Linear -> length()
        is Quadratic -> length(nonlinearityThreshold)
        is Cubic -> length(nonlinearityThreshold)
    }
}
