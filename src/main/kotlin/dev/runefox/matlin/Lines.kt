package dev.runefox.matlin

import kotlin.math.*

inline fun angularBisector(
    ax: Double, ay: Double,
    bx: Double, by: Double,
    res: (Double, Double) -> Unit
) {
    val alen = sqrt(ax * ax + ay * ay)
    val blen = sqrt(bx * bx + by * by)

    val anx = ax / alen
    val any = ay / alen
    val bnx = bx / blen
    val bny = by / blen

    res(lerp(anx, bnx, 0.5), lerp(any, bny, 0.5))
}

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
    val det = (ax - bx) * (py - qy) - (ay - by) * (px - qx)
    if (abs(det) < 1e-20)
        return false

    val ab = ((ax - px) * (py - qy) - (ay - py) * (px - qx)) / det
    val pq = ((ax - px) * (ay - by) - (ay - py) * (ax - bx)) / det
    res(ab, pq)
    return true
}

@Suppress("NOTHING_TO_INLINE") // Having it inline reduces just a little bit of overhead
inline fun projectOnLine(
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    px: Double,
    py: Double
): Double {
    val dx = bx - ax
    val dy = by - ay

    val rx = px - ax
    val ry = py - ay

    val ll = dx * dx + dy * dy
    return (rx * (dx / ll) + ry * (dy / ll))
}

inline fun intersectLineCircle(
    ax: Double,
    ay: Double,
    bx: Double,
    by: Double,
    cx: Double,
    cy: Double,
    r: Double,
    res: (Double, Double, Double, Double) -> Unit
): Boolean {
    // Vector: A to B
    val dx = bx - ax
    val dy = by - ay

    val ll = dx * dx + dy * dy
    val l = sqrt(ll)

    // Vector: A to center
    val rx = cx - ax
    val ry = cy - ay

    // Unit vector in direction of line (tangent)
    val tx = dx / l
    val ty = dy / l

    // How far is the center of the circle away from the line?
    val det = (rx * ty - ry * tx)

    // Too far away, so no intersection
    if (abs(det) > r)
        return false

    // There are intersections, compute how far they are apart (divided by 2 actually)
    // Function of a circular arc of radius r:
    // f(x) = sqrt(r^2 - x^2)
    val sep = sqrt(r*r - det*det)

    // Project circle center on line
    val dot = (rx * tx + ry * ty)

    val t1 = (dot - sep) / l
    val t2 = (dot + sep) / l

    // sep/r = sin(a + la), where la is the angle of clockwise the line

    val la = atan2(dy, dx) - PI/2

    val cos = -det/r
    var a1 = -acos(cos) + la
    var a2 = acos(cos) + la

    if (a1 < 0) a1 += 2*PI
    if (a2 < 0) a2 += 2*PI

    if (a1 < 0) a1 += 2*PI
    if (a2 < 0) a2 += 2*PI

    res(t1, t2, a1, a2)
    return true
}
