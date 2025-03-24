package dev.runefox.matlin

import kotlin.math.*

/**
 * Approximates an arc of the unit circle between points S and E. S and E must lay on the unit circle. The arc returned
 * is always the smallest arc between the points.
 */
private fun approximateUnitArc(sx: Double, sy: Double, ex: Double, ey: Double): Cubic {
    val q1 = sx * sx + sy * sy
    val q2 = q1 + sx * ex + sy * ey
    val k2 = (4.0 / 3.0) * (sqrt(2 * q1 * q2) - q2) / (sx * ey - sy * ex)
    val ax = sx - k2 * sy
    val ay = sy + k2 * sx
    val bx = ex + k2 * ey
    val by = ey - k2 * ex

    return Cubic(sx, sy, ax, ay, bx, by, ex, ey)
}

/**
 * Internal and inlineable version of [approximateArc]. Instead of outputting a list of curves, it will generate curves
 * and pass them to `result`.
 */
internal inline fun approximateArcInternal(
    centerX: Double, centerY: Double,
    radius: Double,
    fromAngle: Double, toAngle: Double,
    accuracy: Int, reverse: Boolean,
    result: (Cubic) -> Unit
) {
    // Correct angles
    var from = fromAngle % (2 * PI)
    var to = toAngle % (2 * PI)

    // Modulo for some reason returns a negative value when input is negative,
    // we must account for that
    if (from < 0) from += 2 * PI
    if (to < 0) to += 2 * PI

    if (fromAngle == toAngle || from == to) {
        // Full circle
        to = from + 2 * PI
    } else if (to < from) {
        to += 2 * PI
    }
    // Now we have:
    // - to > from
    // - from < 2*PI
    // - to <= from + 2*PI

    // Correct accuracy
    val acc = if (accuracy < 2) 2 else accuracy

    // Sweep angle
    val sweep = to - from

    // Amount of subdivisions and subdivision size
    val steps = ceil(acc * sweep / (2 * PI)).toInt()

    // Determine walking direction in terms of a step angle and a start
    // angle
    val step = sweep / steps * if (reverse) -1 else 1
    val start = if (reverse) to else from

    var lsin = sin(start)
    var lcos = cos(start)

    for (i in 1..steps) {
        val cur = start + step * i

        val csin = sin(cur)
        val ccos = cos(cur)

        // Approximate a unit arc and then scale it up
        result(approximateUnitArc(lcos, lsin, ccos, csin).apply {
            sx = sx * radius + centerX
            sy = sy * radius + centerY
            ax = ax * radius + centerX
            ay = ay * radius + centerY
            bx = bx * radius + centerX
            by = by * radius + centerY
            ex = ex * radius + centerX
            ey = ey * radius + centerY
        })

        lsin = csin
        lcos = ccos
    }
}

/**
 * Returns a list of [Cubic] Bézier curves that approximate a circular arc. The arc sweeps counterclockwise
 * between `fromAngle` and `toAngle` in a coordinate system where X is to the right and Y is upwards.
 *
 * The arc sweeps around the center point defined by `centerX` and `centerY`, and it's radius is, unsurprisingly,
 * determined by the `radius` parameter.
 * Typically, `radius` should not be negative, but the method will not check for it and it will simply approximate the
 * arc as if all points as if `PI` was added to both angles. When `radius` is zero, all the curves will be point curves.
 * It will still return the amount of curves that it would return if `radius` where not zero.
 *
 * The angle swept out is determined by `fromAngle` and `toAngle`. These angles are given in radians, but they can be
 * set to any value. Values outside the range `0 .. 2*PI` will simply be wrapped within this range, e.g. `3.5 * PI` will
 * be interpreted as `1.5 * PI`. An angle of `0` radians points towards positive X, and higher values sweep that angle
 * counterclockwise (if Y is upwards).
 * The arc is traced counterclockwise from `fromAngle` to `toAngle` (wrapped). If `toAngle` is less than `fromAngle`,
 * the arc will be traced beyond `2*PI` radians until it hits `toAngle + 2*PI`. When the angles are equal, a full circle
 * will be traced out, starting from `fromAngle`.
 *
 * The final list of curves will follow the arc counterclockwise, but this can be changed by setting the `reverse`
 * parameter. When `reverse` is set to `true`, the returned curves will follow the traced arc clockwise. This parameter
 * does **not** influence the arc that is swept out, only the direction of the curves, i.e. the arc will look the same
 * no matter this value.
 *
 * The `accuracy` parameter sets, as the name suggests, an accuracy level of the circular arc. The value of this
 * parameter determines how much an arc gets subdivided. These subdivisions are then approximated with curves.
 * More specifically, the `accuracy` parameter determines how much arcs to subdivide a full circle into. If the input
 * arc sweeps less than a full circle, it may have less subdivisions, but each subdivision will have at most an arc
 * length of `2 * PI / accuracy`.
 * Smaller arclengths are easier to approximate, so more subdivisions give more accurate results at the cost of speed
 * and memory usage. The time and memory complexity of this algorithm are linearly related to this parameter.
 * An accuracy of 4 already creates splines that are indistinguishable from a real circular arc. Accuracies less than 2
 * will be interpreted as 2, since approximating a circle with 1 curve is not possible.
 *
 * @param centerX   The X coordinate of the center of the arc.
 * @param centerY   The Y coordinate of the center of the arc.
 * @param radius    The radius of the arc.
 * @param fromAngle The starting angle of the arc, in radians. Default is `0.0`.
 * @param toAngle   The ending angle of the arc, in radians. Default is `0.0`.
 * @param accuracy  The accuracy of the arc approximation. Default is `4`.
 * @param reverse   Whether to reverse the direction of the curves or not. Default is `false`.
 * @param out       A list instance to modify. It will be cleared by the function. Default is a new list.
 */
fun approximateArc(
    centerX: Double, centerY: Double,
    radius: Double,
    fromAngle: Double = 0.0, toAngle: Double = 0.0,
    accuracy: Int = 4, reverse: Boolean = false,
    out: MutableList<Cubic> = mutableListOf()
): MutableList<Cubic> {
    out.clear()

    approximateArcInternal(centerX, centerY, radius, fromAngle, toAngle, accuracy, reverse) {
        out += it
    }

    return out
}
