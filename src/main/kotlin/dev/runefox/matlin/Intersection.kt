package dev.runefox.matlin

/**
 * A single point of intersection between two Bézier curves. One curve is represented in terms of a variable `t`, and
 * the other in terms of a variable `u`.
 *
 * For a list of intersections, see [Intersections].
 */
data class Intersection(
    /**
     * The interpolation factor of the first curve at which the intersection happened.
     */
    var t: Double = 0.0,

    /**
     * The interpolation factor of the second curve at which the intersection happened.
     */
    var u: Double = 0.0,

    /**
     * The X coordinate of the intersection.
     */
    var x: Double = 0.0,

    /**
     * The Y coordinate of the intersection.
     */
    var y: Double = 0.0
) {
    fun set(t: Double = this.t, u: Double = this.u, x: Double = this.x, y: Double = this.y) {
        this.t = t
        this.u = u
        this.x = x
        this.y = y
    }

    fun set(isc: Intersection) {
        set(isc.t, isc.u, isc.x, isc.y)
    }
}
