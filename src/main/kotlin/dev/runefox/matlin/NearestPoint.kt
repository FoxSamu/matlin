package dev.runefox.matlin

fun Linear.nearestPoint(
    px: Double,
    py: Double
): Double {
    return clamp01(projectOnLine(sx, sy, ex, ey, px, py))
}

fun Quadratic.nearestPoint(
    px: Double,
    py: Double,
    nonlinearityThreshold: Double = defThreshold
): Double {
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        // Linear case
        return clamp01(projectOnLine(sx, sy, ex, ey, px, py))
    }

    val (sx, sy, ax, ay, ex, ey) = this

    cutStart(0.5)
    val t1 = nearestPoint(px, py, nonlinearityThreshold)
    val x1 = x(t1) - px
    val y1 = y(t1) - py
    val s1 = x1 * x1 + y1 * y1 // No need to do sqrt
    set(sx, sy, ax, ay, ex, ey)

    cutEnd(0.5)
    val t2 = nearestPoint(px, py, nonlinearityThreshold)
    val x2 = x(t2) - px
    val y2 = y(t2) - py
    val s2 = x2 * x2 + y2 * y2 // No need to do sqrt
    set(sx, sy, ax, ay, ex, ey)

    return if (s1 < s2)
        lerp(0.0, 0.5, t1)
    else
        lerp(0.5, 1.0, t2)
}

fun Cubic.nearestPoint(
    px: Double,
    py: Double,
    nonlinearityThreshold: Double = defThreshold
): Double {
    val nl = nonlinearity()
    if (nl < nonlinearityThreshold) {
        // Linear case
        return clamp01(projectOnLine(sx, sy, ex, ey, px, py))
    }

    val (sx, sy, ax, ay, bx, by, ex, ey) = this

    cutStart(0.5)
    val t1 = nearestPoint(px, py, nonlinearityThreshold)
    val x1 = x(t1) - px
    val y1 = y(t1) - py
    val s1 = x1 * x1 + y1 * y1 // No need to do sqrt
    set(sx, sy, ax, ay, bx, by, ex, ey)

    cutEnd(0.5)
    val t2 = nearestPoint(px, py, nonlinearityThreshold)
    val x2 = x(t2) - px
    val y2 = y(t2) - py
    val s2 = x2 * x2 + y2 * y2 // No need to do sqrt
    set(sx, sy, ax, ay, bx, by, ex, ey)

    return if (s1 < s2)
        lerp(0.0, 0.5, t1)
    else
        lerp(0.5, 1.0, t2)
}

fun Bezier.nearestPoint(
    px: Double,
    py: Double,
    nonlinearityThreshold: Double = defThreshold
): Double {
    return when (this) {
        is Linear -> nearestPoint(px, py)
        is Quadratic -> nearestPoint(px, py, nonlinearityThreshold)
        is Cubic -> nearestPoint(px, py, nonlinearityThreshold)
    }
}
