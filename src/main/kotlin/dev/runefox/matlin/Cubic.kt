package dev.runefox.matlin

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * A Bézier curve of degree 3.
 */
data class Cubic(
    override var sx: Double = 0.0,
    override var sy: Double = 0.0,
    var ax: Double = 0.0,
    var ay: Double = 0.0,
    var bx: Double = 0.0,
    var by: Double = 0.0,
    override var ex: Double = 0.0,
    override var ey: Double = 0.0
) : Bezier {
    fun set(
        sx: Double = this.sx,
        sy: Double = this.sy,
        ax: Double = this.ax,
        ay: Double = this.ay,
        bx: Double = this.bx,
        by: Double = this.by,
        ex: Double = this.ex,
        ey: Double = this.ey
    ) {
        this.sx = sx
        this.sy = sy
        this.ax = ax
        this.ay = ay
        this.bx = bx
        this.by = by
        this.ex = ex
        this.ey = ey
    }

    // Reverse engineering a bezier curve from a polynomial ax^3+bx^2+cx+d:

    // System of equations:
    //   S = d
    //   3(A - S) = c
    //   3(B - A) - 3(A - S) = b
    //   E - S - 3(B - A) = a
    //
    // Trivially: S = d, thus we have
    //   3(A - d) = c
    //   A - d = c/3
    //   A = c/3 + d
    //
    // Now we can solve the third equation
    //   3(B - A) - 3(A - S) = b
    //   3((B - A) - (A - S)) = b
    //   3(B - 2A + S) = b
    //   B - 2A + S = b/3
    //   B - 2(c/3 + d) + d = b/3
    //   B - (2c/3 + 2d) + d = b/3
    //   B - 2c/3 - d = b/3
    //   B = b/3 + 2c/3 + d
    //   B = (b + 2c)/3 + d
    //
    // Now we can solve the fourth equation
    //   E - S - 3(B - A) = a
    //   E - S - (3B - 3A) = a
    //   E - S - 3B + 3A = a
    //   E - d - (b + 2c + 3d) + (c + 3d) = a
    //   E - d - b - 2c - 3d + c + 3d = a
    //   E - d - b - c = a
    //   E = a + b + c + d

    /**
     * Sets the curve control points in such a way that the X coordinate of the curve follows the polynomial
     * `x = at^3 + bt^2 + ct + d`. This only modifies the X coordinates of the control points.
     */
    fun setXPolynomial(a: Double, b: Double, c: Double, d: Double) {
        sx = d
        ax = c / 3 + d
        bx = (b + 2 * c) / 3 + d
        ex = a + b + c + d
    }

    /**
     * Sets the curve control points in such a way that the Y coordinate of the curve follows the polynomial
     * `y = at^3 + bt^2 + ct + d`. This only modifies the Y coordinates of the control points.
     */
    fun setYPolynomial(a: Double, b: Double, c: Double, d: Double) {
        sy = d
        ay = c / 3 + d
        by = (b + 2 * c) / 3 + d
        ey = a + b + c + d
    }

    fun setLinear(linear: Linear) {
        setXPolynomial(0.0, 0.0, linear.x1, linear.x0)
        setYPolynomial(0.0, 0.0, linear.y1, linear.y0)
    }

    fun setQuadratic(quad: Quadratic) {
        setXPolynomial(0.0, quad.x2, quad.x1, quad.x0)
        setYPolynomial(0.0, quad.y2, quad.y1, quad.y0)
    }

    fun set(of: Cubic) {
        set(
            of.sx, of.sy,
            of.ax, of.ay,
            of.bx, of.by,
            of.ex, of.ey
        )
    }

    fun drag(
        t: Double,
        px: Double,
        py: Double,
        u: Double = t // factor how much the second handle should be affected in proportion to the first handle
    ) {
        // (E-S-3(B-A))ttt + (3(B-A)-3(A-S))tt + 3(A-S)t + S = P
        // (E-S)ttt - 3(B-A)ttt + 3(B-2A)tt + 3Stt + 3At - 3St + S = P

        // - 3(B-A)ttt + 3(B-2A)tt + 3At + (E-S)ttt + 3Stt - 3St + S = P
        // - 3(B-A)ttt + 3(B-2A)tt + 3At = P - (E-S)ttt - 3Stt + 3St - S
        // - 3Bttt + 3Attt + 3Btt - 6Att + 3At = P - (E-S)ttt - 3Stt + 3St - S
        // - 3Bttt + 3Btt + 3Attt - 6Att + 3At = P - (E-S)ttt - 3Stt + 3St - S

        // - Bttt + Btt + Attt - 2Att + At = (P - (E-S)ttt - 3Stt + 3St - S) / 3
        // - (Bttt - Btt) + (Attt - 2Att + At) = (P - (E-S)ttt - 3Stt + 3St - S) / 3
        // - (Btt - Bt)t + (Att - 2At + A)t = (P - (E-S)ttt - 3Stt + 3St - S) / 3
        // - (Btt - Bt) + (Att - 2At + A) = (P - (E-S)ttt - 3Stt + 3St - S) / 3t
        // - B(tt - t) + A(tt - 2t + 1) = (P - (E-S)ttt - 3Stt + 3St - S) / 3t
        // - Bt(t - 1) + A(t - 1)(t - 1) = (P - (E-S)ttt - 3Stt + 3St - S) / 3t

        // Bt + A(1 - t) = (P - (E-S)ttt - 3Stt + 3St - S) / (-3t(t - 1))

        // So on the line between A and B, we can fix some point in place. However,
        // there is still infinite freedom since many lines go through this point.
        //
        // Let's call this point I:
        // I = (P - (E-S)ttt - 3Stt + 3St - S) / (-3t(t - 1))
        //
        // Let's look at the line segment AB. This is the segment that we
        // want to move. When we compute the point at t along this line, we get
        // the point J, which we want to move to I.
        //
        // We consider u to be a variable that indicates how much we want to
        // move A in proportion to B. When this is 0.5, A and B move equally,
        // when it is 0, only A moves, and when it is 1, only B moves. Naturally,
        // if we grab the curve at a low t, we want A to move primarily, whereas
        // if we grab the curve at a high t, we want B to move primarily. So for
        // a natural effect we set u = t. However, we allow u to be set
        // individually from t.
        //
        // What we do now is the following:
        // At u = 0, we want B to stay fixed in place, thus, B = B, and we can
        // compute A from this as the proportions have to stay the same. We have:
        // Bt + A(1 - t) = I
        // A(1 - t) = I - Bt
        // A = (I - Bt)/(1 - t)
        //
        // At u = 1, we want A to stay fixed in place, thus, A = A. Same thing:
        // Bt + A(1 - t) = I
        // Bt = I - A(1 - t)
        // B = (I - A(1 - t))/t
        //
        // If we now interpolate these two cases given u, we have:
        // A' = ((I - Bt) / (1 - t))(1 - u)   +   Au
        // B' = ((I - A(1 - t)) / t)u   +   B(1 - u)
        //
        // Now if we plug in u = t:
        // A' = ((I - Bt) / (1 - t))(1 - t)   +   At
        // A' = I - Bt + At
        //
        // B' = ((I - A(1 - t)) / t)t   +   B(1 - t)
        // B' = I - A(1 - t) + B(1 - t)
        // B' = I - Bt + At + B - A
        //
        // It's now easy to se that B' - A' = B - A in this case, so this corresponds
        // to moving the A and B by an equal amount. This is exactly what we want
        // at u = 0.5, but we have u = t.
        //
        // Thus we need to map any value of u between 0 and 0.5 to a value between
        // 0 and t, and any value between 0.5 and 1 to a value between t and 1.
        // We do this simply by linear interpolation.

        val div = (-3 * t * (t - 1))
        val tt = t * t
        val ttt = tt * t
        val it = 1 - t

        val ix = (px - (ex - sx) * ttt - 3 * sx * tt + 3 * sx * t - sx) / div
        val iy = (py - (ey - sy) * ttt - 3 * sy * tt + 3 * sy * t - sy) / div

        val r = 2 * u - 1
        val v = if (r < 0) {
            lerp(t, 0.0, -r)
        } else {
            lerp(t, 1.0, r)
        }

        val iv = 1 - v

        val nax = ((ix - bx * t) / it) * iv + ax * v
        val nay = ((iy - by * t) / it) * iv + ay * v

        val nbx = ((ix - ax * it) / t) * v + bx * iv
        val nby = ((iy - ay * it) / t) * v + by * iv

        ax = nax
        ay = nay
        bx = nbx
        by = nby
    }

    internal inline val tbax get() = 3 * (bx - ax)
    internal inline val tasx get() = 3 * (ax - sx)
    internal inline val tbay get() = 3 * (by - ay)
    internal inline val tasy get() = 3 * (ay - sy)

    override val x0 get() = sx
    override val x1 get() = tasx
    override val x2 get() = tbax - tasx
    override val x3 get() = ex - sx - tbax

    override val y0 get() = sy
    override val y1 get() = tasy
    override val y2 get() = tbay - tasy
    override val y3 get() = ey - sy - tbay

    override val xPolynomial = CubicPolynomial()
        get() = field.apply {
            a0 = x0
            a1 = x1
            a2 = x2
            a3 = x3
        }

    override val yPolynomial = CubicPolynomial()
        get() = field.apply {
            a0 = y0
            a1 = y1
            a2 = y2
            a3 = y3
        }



    override fun x(t: Double): Double {
        val sx = sx // Make local for faster access
        val tba = tbax
        val tas = tasx
        return (((ex - sx - tba) * t + (tba - tas)) * t + tas) * t + sx
    }

    override fun y(t: Double): Double {
        val sy = sy // Make local for faster access
        val tba = tbay
        val tas = tasy
        return (((ey - sy - tba) * t + (tba - tas)) * t + tas) * t + sy
    }

    override fun dx(t: Double): Double {
        val sx = sx // Make local for faster access
        val tba = tbax
        val tas = tasx
        return (3 * (ex - sx - tba) * t + 2 * (tba - tas)) * t + tas
    }

    override fun dy(t: Double): Double {
        val sy = sy // Make local for faster access
        val tba = tbay
        val tas = tasy
        return (3 * (ey - sy - tba) * t + 2 * (tba - tas)) * t + tas
    }

    override fun ddx(t: Double): Double {
        val sx = sx // Make local for faster access
        val ax = ax
        val ba = (bx - ax)
        return 6 * ((ex - sx - 3 * ba) * t + (ba - (ax - sx)))
    }

    override fun ddy(t: Double): Double {
        val sy = sy // Make local for faster access
        val ay = ay
        val ba = (by - ay)
        return 6 * ((ey - sy - 3 * ba) * t + (ba - (ay - sy)))
    }

    /**
     * Computes the derivative curve of this curve. The derivative of a degree `N` Bézier curve (`N >= 2`) is itself a
     * Bézier curve of degree `N-1`. That is, the returned curve should be a [Quadratic]. This function accepts a curve
     * instance as parameter, which it will modify.
     */
    fun derive(out: Quadratic = Quadratic()): Quadratic {
        val dx0 = x1
        val dx1 = 2 * x2
        val dx2 = 3 * x3

        val dy0 = y1
        val dy1 = 2 * y2
        val dy2 = 3 * y3

        out.setXPolynomial(dx2, dx1, dx0)
        out.setYPolynomial(dy2, dy1, dy0)
        return out
    }

    /**
     * Computes the second derivative curve of this curve. The second derivative of a degree `N` Bézier curve (`N >= 3`) is itself a
     * Bézier curve of degree `N-2`. That is, the returned curve should be a [Linear]. This function accepts a curve
     * instance as parameter, which it will modify.
     */
    fun deriveTwice(out: Linear = Linear()): Linear {
        val ddx0 = 2 * x2
        val ddx1 = 6 * x3

        val ddy0 = 2 * y2
        val ddy1 = 6 * y3

        out.setXPolynomial(ddx1, ddx0)
        out.setYPolynomial(ddy1, ddy0)
        return out
    }

    override fun boundingBox(out: BoundingBox): BoundingBox {
        out.minX = min(sx, ex)
        out.minY = min(sy, ey)
        out.maxX = max(sx, ex)
        out.maxY = max(sy, ey)

        val tbax = tbax
        val tasx = tasx
        solveQuadratic(3 * (ex - sx - tbax), 2 * (tbax - tasx), tasx) { pt ->
            if (pt >= 0 && pt <= 1) {
                val x = x(pt)
                if (x < out.minX) out.minX = x
                if (x > out.maxX) out.maxX = x
            }
        }

        val tbay = tbay
        val tasy = tasy
        solveQuadratic(3 * (ey - sy - tbay), 2 * (tbay - tasy), tasy) { pt ->
            if (pt >= 0 && pt <= 1) {
                val y = y(pt)
                if (y < out.minY) out.minY = y
                if (y > out.maxY) out.maxY = y
            }
        }

        return out
    }

    override fun duplicate() = copy()

    override fun reverse() {
        set(ex, ey, bx, by, ax, ay, sx, sy)
    }

    override fun cutStart(t: Double) {
        cut(t, start = this)
    }

    override fun cutEnd(t: Double) {
        cut(t, end = this)
    }

    override fun nonlinearity(): Double {
        // A cubic curve is linear if its control points sit on the line between start and end at exactly
        // 1/3rd and 2/3rd.
        // For both control points we compute a third the distance from where it is in its linear position, and then
        // sum those

        val cx = (ex - sx) * 0.333333333333333333333333 + sx - ax
        val cy = (ey - sy) * 0.333333333333333333333333 + sy - ay

        val dx = (ex - sx) * 0.666666666666666666666666 + sx - bx
        val dy = (ey - sy) * 0.666666666666666666666666 + sy - by

        return sqrt(cx * cx + cy * cy) / 3 + sqrt(dx * dx + dy * dy) / 3
    }

    fun cut(t: Double, start: Cubic? = null, end: Cubic? = null) {
        val x = x(t)
        val y = y(t)

        val (sx, sy, ax, ay, bx, by, ex, ey) = this

        if (start != null) {
            val px = lerp(sx, ax, t)
            val py = lerp(sy, ay, t)

            val qx = lerp(ax, bx, t)
            val qy = lerp(ay, by, t)

            start.sx = sx
            start.sy = sy
            start.ax = px
            start.ay = py
            start.bx = lerp(px, qx, t)
            start.by = lerp(py, qy, t)
            start.ex = x
            start.ey = y
        }

        if (end != null) {
            val px = lerp(ax, bx, t)
            val py = lerp(ay, by, t)

            val qx = lerp(bx, ex, t)
            val qy = lerp(by, ey, t)

            end.sx = x
            end.sy = y
            end.ax = lerp(px, qx, t)
            end.ay = lerp(py, qy, t)
            end.bx = qx
            end.by = qy
            end.ex = ex
            end.ey = ey
        }
    }

    fun through4Points(
        x1: Double, y1: Double, t1: Double,
        x2: Double, y2: Double, t2: Double,
        x3: Double, y3: Double, t3: Double,
        x4: Double, y4: Double, t4: Double,
    ) {
        solveLinearSystem(
            (1-t1)*(1-t1)*(1-t1), 3*(1-t1)*(1-t1)*t1, 3*(1-t1)*t1*t1, t1*t1*t1,
            (1-t2)*(1-t2)*(1-t2), 3*(1-t2)*(1-t2)*t2, 3*(1-t2)*t2*t2, t2*t2*t2,
            (1-t3)*(1-t3)*(1-t3), 3*(1-t3)*(1-t3)*t3, 3*(1-t3)*t3*t3, t3*t3*t3,
            (1-t4)*(1-t4)*(1-t4), 3*(1-t4)*(1-t4)*t4, 3*(1-t4)*t4*t4, t4*t4*t4,
            x1, x2, x3, x4
        ) { u1, u2, u3, u4 ->
            sx = u1
            ax = u2
            bx = u3
            ex = u4
        }

        solveLinearSystem(
            (1-t1)*(1-t1)*(1-t1), 3*(1-t1)*(1-t1)*t1, 3*(1-t1)*t1*t1, t1*t1*t1,
            (1-t2)*(1-t2)*(1-t2), 3*(1-t2)*(1-t2)*t2, 3*(1-t2)*t2*t2, t2*t2*t2,
            (1-t3)*(1-t3)*(1-t3), 3*(1-t3)*(1-t3)*t3, 3*(1-t3)*t3*t3, t3*t3*t3,
            (1-t4)*(1-t4)*(1-t4), 3*(1-t4)*(1-t4)*t4, 3*(1-t4)*t4*t4, t4*t4*t4,
            y1, y2, y3, y4
        ) { u1, u2, u3, u4 ->
            sy = u1
            ay = u2
            by = u3
            ey = u4
        }
    }
}
