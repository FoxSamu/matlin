package dev.runefox.matlin

import kotlin.math.max
import kotlin.math.min

/**
 * Linearly interpolates the value `t` between `a` and `b`. That is, at `t = 0` it will return `a` and at `t = 1` it
 * will return `b`, and any value of `t` between 0 and 1 will return a value between `a` and `b` at equal proportions.
 * If `t` is outside the range `0..1` it will extrapolate the value linearly.
 */
fun lerp(a: Double, b: Double, t: Double): Double {
    return (b-a) * t + a
}

/**
 * Linearly un-interpolates the value `t` from between `a` and `b`. This is the reverse operation of [lerp]. That is,
 * at `t = a` it will return 0 and at `t = b` it will return 1, and any value of `t` between `a` and `b` will return
 * a value between 0 and 1 at equal proportions. If `t` is outside the range `a..b` it will un-extrapolate the value.
 *
 * A special case is when `a = b`, then this function will return `NaN`.
 */
fun unlerp(a: Double, b: Double, t: Double): Double {
    if (a == b) return Double.NaN
    return (t - a) / (b - a)
}

fun clamp(a: Double, b: Double, t: Double): Double {
    return max(a, min(b, t))
}

fun clamp01(t: Double) = clamp(0.0, 1.0, t)
