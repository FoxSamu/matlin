package dev.runefox.matlin

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * An axis-aligned bounding box.
 */
data class BoundingBox(
    var minX: Double = 0.0,
    var minY: Double = 0.0,
    var maxX: Double = 0.0,
    var maxY: Double = 0.0
) {
    infix fun overlaps(other: BoundingBox): Boolean {
        return minX <= other.maxX
            && maxX >= other.minX
            && minY <= other.maxY
            && maxY >= other.minY
    }

    /**
     * Checks whether the box contains any point on the boundary of the given circle.
     */
    fun intersectsCircle(cx: Double, cy: Double, r: Double): Boolean {
        fun d2(x: Double, y: Double, cx: Double, cy: Double): Double {
            val dx = x - cx
            val dy = y - cy
            return dx * dx + dy * dy
        }

        val d1 = d2(minX, minY, cx, cy)
        val d2 = d2(maxX, minY, cx, cy)
        val d3 = d2(maxX, maxY, cx, cy)
        val d4 = d2(minX, maxY, cx, cy)

        val r2 = r*r

        // If all points are inside the box cannot intersect
        if (d1 < r2 && d2 < r2 && d3 < r2 && d4 < r2)
            return false


        // If all points are outside the box can only intersect if the center
        // is in one of the extents of the box
        if (d1 >= r2 && d2 >= r2 && d3 >= r2 && d4 >= r2) {
            val hd = min(abs(cx - minX), abs(cx - maxX))
            val vd = min(abs(cy - minY), abs(cy - maxY))

            val icx = cx > minX && cx < maxX
            val icy = cy > minY && cy < maxY
            val xr = icx && vd < r
            val yr = icy && hd < r
            return xr || yr || (icx && icy)
        }

        // Some points are inside, some are not, thus there must be an intersection
        return true
    }
}
