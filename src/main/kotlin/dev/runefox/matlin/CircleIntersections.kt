@file:Suppress("ConvertTwoComparisonsToRangeCheck") // Ranges are objects which create overhead

package dev.runefox.matlin

import javax.swing.text.html.HTML.Tag.I
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


private fun isOnCircle(x: Double, y: Double, cx: Double, cy: Double, r: Double): Boolean {
    val dx = x - cx
    val dy = y - cy

    if (abs(sqrt(dx * dx + dy * dy) - r) < 1e-20)
        return true
    return false
}

private fun intersectCircle0(
    st: Double,
    et: Double,
    curve: Bezier,
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections
): Int {
    val dx = bx - ax
    val dy = by - ay
    if (abs(dx) < 1e-17 && abs(dy) < 1e-17) {
        val rx = ax - cx
        val ry = ay - cy

        if ((rx * rx + ry * ry) - r * r < 1e-17) {
            var angle = atan2(ry, rx)
            if (angle < 0) angle += 2 * PI

            iscs.add(
                st,
                angle,
                ax,
                ay
            )
        }



        return 0
    }

    val res = intersectLineCircle(ax, ay, bx, by, cx, cy, r) { t1, t2, a1, a2 ->
        if (t1 >= 0 && t1 < 1)
            iscs.add(lerp(st, et, t1), a1, curve.x(t1), curve.y(t1))

        if (t2 >= 0 && t2 < 1)
            iscs.add(lerp(st, et, t2), a2, curve.x(t2), curve.y(t2))
    }
    return if (res) 2 else 0
}

fun Linear.intersectCircle(
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections
): Int {
    iscs.clear()
    return intersectCircle0(0.0, 1.0, this, sx, sy, ex, ey, cx, cy, r, iscs)
}

private fun Quadratic.intersectCircle0(
    st: Double,
    et: Double,
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections,
    bbox: BoundingBox,
    nonlinearityThreshold: Double
): Int {
    boundingBox(bbox)
    if (!bbox.intersectsCircle(cx, cy, r))
        return 0

    val nonlinearity = nonlinearity()
    if (nonlinearity < nonlinearityThreshold) {
        val res = intersectLineCircle(sx, sy, ex, ey, cx, cy, r) { t1, t2, a1, a2 ->
            if (t1 >= 0 && t1 < 1)
                iscs.add(lerp(st, et, t1), a1, x(t1), y(t1))

            if (t2 >= 0 && t2 < 1)
                iscs.add(lerp(st, et, t2), a2, x(t2), y(t2))
        }
        return if (res) 2 else 0
    }

    val (sx, sy, ax, ay, ex, ey) = this

    // If we cut at a point that is on the circle, we will get very odd results.
    // Try to avoid picking a t that lands exactly on the circle
    var t = 0.5
    var divs = 2
    var j = 0
    while (j < 10) {
        var i = 1
        while (i < divs) {
            t = i.toDouble() / divs
            if (!isOnCircle(x(t), y(t), cx, cy, r)) {
                j = 10
                break
            }
            i++
        }
        divs *= 2
        j++
    }

    val mt = lerp(st, et, t)
    cutStart(t)
    val ai = intersectCircle0(st, mt, cx, cy, r, iscs, bbox, nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    cutEnd(t)
    val bi = intersectCircle0(mt, et, cx, cy, r, iscs, bbox, nonlinearityThreshold)
    set(sx, sy, ax, ay, ex, ey)

    return ai + bi
}

fun Quadratic.intersectCircle(
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
): Int {
    iscs.clear()
    return intersectCircle0(0.0, 1.0, cx, cy, r, iscs, BoundingBox(), nonlinearityThreshold)
}

private fun Cubic.intersectCircle0(
    st: Double,
    et: Double,
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections,
    bbox: BoundingBox,
    nonlinearityThreshold: Double
): Int {
    boundingBox(bbox)
    if (!bbox.intersectsCircle(cx, cy, r))
        return 0

    val nonlinearity = nonlinearity()
    if (nonlinearity < nonlinearityThreshold) {
        val res = intersectLineCircle(sx, sy, ex, ey, cx, cy, r) { t1, t2, a1, a2 ->
            if (t1 >= 0 && t1 < 1)
                iscs.add(lerp(st, et, t1), a1, x(t1), y(t1))

            if (t2 >= 0 && t2 < 1)
                iscs.add(lerp(st, et, t2), a2, x(t2), y(t2))
        }
        return if (res) 2 else 0
    }

    val (sx, sy, ax, ay, bx, by, ex, ey) = this

    // If we cut at a point that is on the circle, we will get very odd results.
    // Try to avoid picking a t that lands exactly on the circle
    var t = 0.5
    var divs = 2
    var j = 0
    while (j < 10) {
        var i = 1
        while (i < divs) {
            t = i.toDouble() / divs
            if (!isOnCircle(x(t), y(t), cx, cy, r)) {
                j = 10
                break
            }
            i++
        }
        divs *= 2
        j++
    }

    val mt = lerp(st, et, t)
    cutStart(t)
    val ai = intersectCircle0(st, mt, cx, cy, r, iscs, bbox, nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    cutEnd(t)
    val bi = intersectCircle0(mt, et, cx, cy, r, iscs, bbox, nonlinearityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    return ai + bi
}

fun Cubic.intersectCircle(
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
): Int {
    iscs.clear()
    return intersectCircle0(0.0, 1.0, cx, cy, r, iscs, BoundingBox(), nonlinearityThreshold)
}

fun Bezier.intersectCircle(
    cx: Double,
    cy: Double,
    r: Double,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
): Int {
    return when (this) {
        is Linear -> intersectCircle(cx, cy, r, iscs)
        is Quadratic -> intersectCircle(cx, cy, r, iscs, nonlinearityThreshold)
        is Cubic -> intersectCircle(cx, cy, r, iscs, nonlinearityThreshold)
    }
}
