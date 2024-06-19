package dev.runefox.matlin

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * A Bézier curve of degree 2, i.e. a parabola.
 */
data class Quadratic(
    override var sx: Double = 0.0,
    override var sy: Double = 0.0,
    var ax: Double = 0.0,
    var ay: Double = 0.0,
    override var ex: Double = 0.0,
    override var ey: Double = 0.0
) : Bezier {
    fun set(
        sx: Double = this.sx,
        sy: Double = this.sy,
        ax: Double = this.ax,
        ay: Double = this.ay,
        ex: Double = this.ex,
        ey: Double = this.ey
    ) {
        this.sx = sx
        this.sy = sy
        this.ax = ax
        this.ay = ay
        this.ex = ex
        this.ey = ey
    }

    // Reverse engineering a bezier curve from a polynomial ax^2+bx+c:

    // System of equations:
    //   S = c
    //   2(A - S) = b
    //   E + S - 2A = a
    //
    // Trivially: S = c, thus we have
    //   2(A - c) = b
    //   A - c = b/2
    //   A = b/2 + c
    //
    // Now we can solve the third equation
    //   E + c - 2(b/2 + c) = a
    //   E + c - b - 2c = a
    //   E - b - c = a
    //   E = a + b + c

    fun setXPolynomial(a: Double, b: Double, c: Double) {
        sx = c
        ax = b / 2 + c
        ex = a + b + c
    }

    fun setYPolynomial(a: Double, b: Double, c: Double) {
        sy = c
        ay = b / 2 + c
        ey = a + b + c
    }

    fun drag(
        t: Double,
        px: Double,
        py: Double
    ) {
        // (E + S - 2A)t^2 + (2A - 2S)t + S = P

        // (E + S - 2A)t^2 + (2A - 2S)t + S = P
        // Et^2 + St^2 - 2At^2 + 2At - 2St + S = P
        // - 2At^2 + 2At = P + 2St - S - Et^2 - St^2
        // - 2(At^2 - At) = P + 2St - S - Et^2 - St^2
        // - 2t(At - A) = P + 2St - S - Et^2 - St^2
        // -2t(t-1)A = P + 2St - S - Et^2 - St^2
        //  A = (P + 2St - S - Et^2 - St^2)/(-2t(t-1))

        val div = (-2 * t * (t - 1))
        val tt = t*t

        ax = (px + 2 * sx * t - sx - (ex + sx) * tt) / div
        ay = (py + 2 * sy * t - sy - (ey + sy) * tt) / div
    }

    fun setLinear(linear: Linear) {
        setXPolynomial(0.0, linear.x1, linear.x0)
        setYPolynomial(0.0, linear.y1, linear.y0)
    }

    fun set(of: Quadratic) {
        set(
            of.sx, of.sy,
            of.ax, of.ay,
            of.ex, of.ey
        )
    }

    override val x0 get() = sx
    override val x1 get() = 2 * (ax - sx)
    override val x2 get() = ex + sx - ax * 2
    override val x3 get() = 0.0

    override val y0 get() = sy
    override val y1 get() = 2 * (ay - sy)
    override val y2 get() = ey + sy - ay * 2
    override val y3 get() = 0.0

    override fun x(t: Double): Double {
        val dx = ax * 2
        val sx = sx // Make it local for faster access
        return ((ex + sx - dx) * t + (dx - 2 * sx)) * t + sx
    }

    override fun y(t: Double): Double {
        val dy = ay * 2
        val sy = sy // Make it local for faster access
        return ((ey + sy - dy) * t + (dy - 2 * sy)) * t + sy
    }

    override fun dx(t: Double): Double {
        val dx = ax * 2
        val sx = sx // Make it local for faster access
        return 2 * (ex + sx - dx) * t + (dx - 2 * sx)
    }

    override fun dy(t: Double): Double {
        val dy = ay * 2
        val sy = sy // Make it local for faster access
        return 2 * (ey + sy - dy) * t + (dy - 2 * sy)
    }

    override fun ddx(t: Double): Double {
        return 2 * (ex + sx - ax * 2)
    }

    override fun ddy(t: Double): Double {
        return 2 * (ey + sy - ay * 2)
    }

    /**
     * Computes the derivative curve of this curve. The derivative of a degree `N` Bézier curve (`N >= 2`) is itself a
     * Bézier curve of degree `N-1`. That is, the returned curve should be a [Linear]. This function accepts a curve
     * instance as parameter, which it will modify.
     */
    fun derive(out: Linear = Linear()): Linear {
        val dx0 = x1
        val dx1 = 2 * x2

        val dy0 = y1
        val dy1 = 2 * y2

        out.setXPolynomial(dx1, dx0)
        out.setYPolynomial(dy1, dy0)
        return out
    }

    /**
     * Computes the integral curve of this curve. The integral of a degree `N` Bézier curve (`N >= 1`) is itself a
     * Bézier curve of degree `N+1`. That is, the returned curve should be a [Cubic]. This function accepts a curve
     * instance as parameter, which it will modify.
     *
     * Note that integrating a function introduces an arbitrary integration constant. In case of a Bézier curve, this
     * integration constant represents the starting point of the curve. By default, it will use the origin, i.e.
     * `[0, 0]`.
     */
    fun integrate(sx: Double = 0.0, sy: Double = 0.0, out: Cubic = Cubic()): Cubic {
        val ix0 = sx
        val ix1 = x0
        val ix2 = x1 / 2
        val ix3 = x1 / 3

        val iy0 = sy
        val iy1 = y0
        val iy2 = y1 / 2
        val iy3 = y1 / 3

        out.setXPolynomial(ix3, ix2, ix1, ix0)
        out.setYPolynomial(iy3, iy2, iy1, iy0)
        return out
    }

    override fun boundingBox(out: BoundingBox): BoundingBox {
        out.minX = min(sx, ex)
        out.minY = min(sy, ey)
        out.maxX = max(sx, ex)
        out.maxY = max(sy, ey)

        val dx = ax * 2
        val dy = ay * 2

        val dx0t = (-dx + 2 * sx) / (2 * (ex + sx - dx))
        val dy0t = (-dy + 2 * sy) / (2 * (ey + sy - dy))

        if (dx0t >= 0 && dx0t <= 1) {
            val x = x(dx0t)
            if (x < out.minX) out.minX = x
            if (x > out.maxX) out.maxX = x
        }

        if (dy0t >= 0 && dy0t <= 1) {
            val y = y(dy0t)
            if (y < out.minY) out.minY = y
            if (y > out.maxY) out.maxY = y
        }
        return out
    }

    override fun duplicate() = copy()

    override fun reverse() {
        set(ex, ey, ax, ay, sx, sy)
    }

    override fun cutStart(t: Double) {
        cut(t, start = this)
    }

    override fun cutEnd(t: Double) {
        cut(t, end = this)
    }

    override fun nonlinearity(): Double {
        // A quadratic curve is linear if its control point is right in between its two endpoints.
        // We use half the distance from the control point to this central point as a measure of nonlinearity.

        val cx = (ex - sx) / 2 + sx - ax
        val cy = (ey - sy) / 2 + sy - ay

        return sqrt(cx * cx + cy * cy) / 2
    }

    fun cut(t: Double, start: Quadratic? = null, end: Quadratic? = null) {
        val x = x(t)
        val y = y(t)

        val (sx, sy, ax, ay, ex, ey) = this

        if (start != null) {
            start.sx = sx
            start.sy = sy
            start.ax = lerp(sx, ax, t)
            start.ay = lerp(sy, ay, t)
            start.ex = x
            start.ey = y
        }

        if (end != null) {
            end.sx = x
            end.sy = y
            end.ax = lerp(ax, ex, t)
            end.ay = lerp(ay, ey, t)
            end.ex = ex
            end.ey = ey
        }
    }
}
