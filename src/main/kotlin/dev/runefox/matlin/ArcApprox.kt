package dev.runefox.matlin

inline fun Cubic.approximate2Arcs(r: (x: Double, y: Double, r: Double, sa: Double, ea: Double) -> Unit): Boolean {
    return intersectLines(sx, sy, ax, ay, ex, ey, bx, by) { u, v ->
        if (u < 0 || v < 0) {
            return false
        }

        val gx = lerp(sx, ax, u)
        val gy = lerp(sy, ay, u)
    }
}
