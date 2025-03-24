package dev.runefox.matlin

import kotlin.math.max
import kotlin.math.min

/**
 * A Bézier curve of degree 1, i.e. a straight line.
 */
data class Linear(
    override var sx: Double = 0.0,
    override var sy: Double = 0.0,
    override var ex: Double = 0.0,
    override var ey: Double = 0.0
) : Bezier {
    fun set(
        sx: Double = this.sx,
        sy: Double = this.sy,
        ex: Double = this.ex,
        ey: Double = this.ey
    ) {
        this.sx = sx
        this.sy = sy
        this.ex = ex
        this.ey = ey
    }

    // Reverse engineering a bezier curve from a polynomial ax+b:

    // System of equations:
    //   S = b
    //   E - S = a
    //
    // Trivially: S = b, thus we have
    //   E - b = a
    //   E = a + b

    fun setXPolynomial(a: Double, b: Double) {
        sx = b
        ex = a + b
    }

    fun setYPolynomial(a: Double, b: Double) {
        sy = b
        ey = a + b
    }

    fun set(of: Linear) {
        set(
            of.sx, of.sy,
            of.ex, of.ey
        )
    }

    override val x0 get() = sx
    override val x1 get() = ex - sx
    override val x2 get() = 0.0
    override val x3 get() = 0.0

    override val y0 get() = sy
    override val y1 get() = ey - sy
    override val y2 get() = 0.0
    override val y3 get() = 0.0

    override val xPolynomial = LinearPolynomial()
        get() = field.apply {
            a0 = x0
            a1 = x1
        }

    override val yPolynomial = LinearPolynomial()
        get() = field.apply {
            a0 = y0
            a1 = y1
        }

    override fun x(t: Double) = (ex - sx) * t + sx
    override fun y(t: Double) = (ey - sy) * t + sy

    override fun dx(t: Double) = ex - sx
    override fun dy(t: Double) = ey - sy

    override fun ddx(t: Double) = 0.0
    override fun ddy(t: Double) = 0.0

    /**
     * Computes the integral curve of this curve. The integral of a degree `N` Bézier curve (`N >= 1`) is itself a
     * Bézier curve of degree `N+1`. That is, the returned curve should be a [Quadratic]. This function accepts a curve
     * instance as parameter, which it will modify.
     *
     * Note that integrating a function introduces an arbitrary integration constant. In case of a Bézier curve, this
     * integration constant represents the starting point of the curve. By default, it will use the origin, i.e.
     * `[0, 0]`.
     */
    fun integrate(sx: Double = 0.0, sy: Double = 0.0, out: Quadratic = Quadratic()): Quadratic {
        val ix0 = sx
        val ix1 = x0
        val ix2 = x1/2

        val iy0 = sy
        val iy1 = y0
        val iy2 = y1/2

        out.setXPolynomial(ix2, ix1, ix0)
        out.setYPolynomial(iy2, iy1, iy0)
        return out
    }

    override fun boundingBox(out: BoundingBox): BoundingBox {
        out.minX = min(sx, ex)
        out.minY = min(sy, ey)
        out.maxX = max(sx, ex)
        out.maxY = max(sy, ey)
        return out
    }

    override fun duplicate() = copy()

    override fun reverse() {
        set(ex, ey, sx, sy)
    }

    override fun cutStart(t: Double) {
        cut(t, start = this)
    }

    override fun cutEnd(t: Double) {
        cut(t, end = this)
    }

    override fun nonlinearity(): Double {
        return 0.0 // We are literally the definition of what it means to be a curve with 0 nonlinearity
    }

    fun cut(t: Double, start: Linear? = null, end: Linear? = null) {
        val x = x(t)
        val y = y(t)

        val (sx, sy, ex, ey) = this

        if (start != null) {
            start.sx = sx
            start.sy = sy
            start.ex = x
            start.ey = y
        }

        if (end != null) {
            end.sx = x
            end.sy = y
            end.ex = ex
            end.ey = ey
        }
    }

    fun through2Points(
        x1: Double, y1: Double, t1: Double,
        x2: Double, y2: Double, t2: Double
    ) {
        solveLinearSystem(
            1-t1, t1,
            1-t2, t2,
            x1, x2,
        ) { u1, u2 ->
            sx = u1
            ey = u2
        }

        solveLinearSystem(
            1-t1, t1,
            1-t2, t2,
            y1, y2,
        ) { u1, u2 ->
            sy = u1
            ey = u2
        }
    }
}
