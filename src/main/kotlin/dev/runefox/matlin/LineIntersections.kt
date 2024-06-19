package dev.runefox.matlin

import kotlin.math.abs

fun Linear.intersectLine(
    px: Double,
    py: Double,
    qx: Double,
    qy: Double,
    iscs: Intersections
): Int {
    iscs.clear()
    val res = intersectLines(sx, sy, ex, ey, px, py, qx, qy) { t, u ->
        iscs.add(t, u, x(t), y(t))
    }
    return if (res) 1 else 0
}

fun Quadratic.intersectLine(
    px: Double,
    py: Double,
    qx: Double,
    qy: Double,
    iscs: Intersections
): Int {
    iscs.clear()
    // The idea is that we want to find the intersections between a degree 2
    // and a degree 1 polynomial. Ideally, you'd just subtract one from the
    // other, set it equal to 0, and solve it.
    //
    // The line is itself a Bézier curve: (Q-P)u + P. Note that we use u here
    // instead of t, these are different variables! So what we need to do is
    // representing our line in terms of t instead of u first - we may not
    // just subtract two polynomials of different variables and call it a day.
    //
    // To do this, we first write our line in terms of x, in the form:
    // y = px + q. This requires the line not to be vertical, but we'll deal
    // with this later. For now we can assume that to be not the case.
    //
    // Our curve has a polynomial definition for x:
    // x = Ax t^2 + Bx t + Cx
    // We can substitute into the line equation, giving:
    // y = p Ax t^2 + p Bx t + (p Cx + q)
    //
    // So by doing this substitution, we have defined a formula for our line
    // in terms of the curve's interpolation factor, t. It is no longer
    // linear, but that's ok, we know how to solve a quadratic polynomial
    // too.
    //
    // We have this polynomial for y defined in terms of t, which is much
    // like the definition for x, but then for y:
    // y = Ay t^2 + By t + Cy
    //
    // Subtracting the line polynomial gives:
    // y = (Ay - p Ax) t^2 + (By - p Bx) t + (Cy - p Cx - q)
    //
    // This is a quadratic polynomial that we can solve for t, giving us the
    // intersection points we were looking for.
    //
    // There is one more thing we need to do though, that is computing the value
    // of u for each intersection. For this we need to express u in terms of t.
    // We have a definition of x in terms of t:
    // x = Ax t^2 + Bx t + Cx
    // We can define our line using a linear interpolation between P and Q as
    // well:
    // x = lerp(Px, Qx, u)
    // Fortunately, we have an inverse of this lerp function:
    // u = unlerp(Px, Qx, x)
    // Once again, this inverse does not work when Px = Qx, i.e. when the line is
    // vertical, but as said before, this is not the case.
    // We have x in terms of t so by substitution we now get:
    // u = unlerp(Px, Qx, Ax t^2 + Bx t + Cx)
    //
    // Since Bézier curves are parametric curves, we can simply mirror the
    // entire situation around the line y=x. That is, we swap all x and y
    // related constants properly. This allows us to handle the vertical
    // case. It also allows us to keep the slope of the line equation to
    // 1 or less, it allows us to save a bit more on float precision.
    //
    // We do this by checking if the line is more vertical than that it is
    // horizontal, i.e. it's Y-derivative is bigger than its X-derivative.
    val vertical = abs(qx - px) < abs(qy - py)
    if (vertical) {
        // Transform to X-polynomial intersecting the Y-axis
        val p = (qx - px) / (qy - py)
        val q = qx - p * qy

        var i = 0
        solveQuadratic(x2 - p * y2, x1 - p * y1, x0 - p * y0 - q) { t ->
            val y = y(t)
            val u = unlerp(py, qy, y)
            iscs.add(t, u, x(t), y)
            i++
        }
        return i
    } else {
        // Transform to Y-polynomial intersecting the X-axis
        val p = (qy - py) / (qx - px)
        val q = qy - p * qx

        var i = 0
        solveQuadratic(y2 - p * x2, y1 - p * x1, y0 - p * x0 - q) { t ->
            val x = x(t)
            val u = unlerp(px, qx, x)
            iscs.add(t, u, x, y(t))
            i++
        }
        return i
    }
}


fun Cubic.intersectLine(
    px: Double,
    py: Double,
    qx: Double,
    qy: Double,
    iscs: Intersections
): Int {
    iscs.clear()
    // Much the same as for quadratic curves, see above.
    //
    // Line equation: y = px + q
    //
    // Curve has a polynomial definition for x:
    // x = Ax t^3 + Bx t^2 + Cx t + Dx
    // Substitute into the line equation:
    // y = p Ax t^3 + p Bx t^2 + p Cx t + (p Dx + q)
    //
    // Subtract line polynomial from polynomial definition for y:
    // y = (Ay - p Ax) t^3 + (By - p Bx) t^2 + (Cy - p Cx) t + (Dy - p Dx - q)
    //
    // That's a cubic polynomial, we can solve that.

    val vertical = abs(qx - px) < abs(qy - py)
    if (vertical) {
        // Transform to X-polynomial intersecting the Y-axis
        val p = (qx - px) / (qy - py)
        val q = qx - p * qy

        var i = 0
        solveCubic(x3 - p * y3, x2 - p * y2, x1 - p * y1, x0 - p * y0 - q) { t ->
            val y = y(t)
            val u = unlerp(py, qy, y)
            iscs.add(t, u, x(t), y)
            i++
        }
        return i
    } else {
        // Transform to Y-polynomial intersecting the X-axis
        val p = (qy - py) / (qx - px)
        val q = qy - p * qx

        var i = 0
        solveCubic(y3 - p * x3, y2 - p * x2, y1 - p * x1, y0 - p * x0 - q) { t ->
            val x = x(t)
            val u = unlerp(px, qx, x)
            iscs.add(t, u, x, y(t))
            i++
        }
        return i
    }
}

fun Bezier.intersectLine(
    px: Double,
    py: Double,
    qx: Double,
    qy: Double,
    iscs: Intersections
): Int {
    return when (this) {
        is Linear -> intersectLine(px, py, qx, qy, iscs)
        is Quadratic -> intersectLine(px, py, qx, qy, iscs)
        is Cubic -> intersectLine(px, py, qx, qy, iscs)
    }
}
