package dev.runefox.matlin

import kotlin.math.abs
import kotlin.math.sqrt

inline fun intersectLines(
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    px: Double,
    py: Double,
    qx: Double,
    qy: Double,
    res: (Double, Double) -> Unit
): Boolean {
    var det = (ax - bx) * (py - qy) - (ay - by) * (px - qx)
    if (abs(det) < 1e-20)
        return false

    val ab = ((ax - px) * (py - qy) - (ay - py) * (px - qx)) / det
    val pq = ((ax - px) * (ay - by) - (ay - py) * (ax - bx)) / det
    res(ab, pq)
    return true
}

fun projectOnLine(
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    px: Double,
    py: Double
): Double {
    val dx = bx - ax
    val dy = by - ay
    val l = dx * dx + dy * dy

    val rx = px - ax
    val ry = py - ay

    return (rx * (dx / l) + ry * (dy / l))
}


