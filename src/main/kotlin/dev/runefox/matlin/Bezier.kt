package dev.runefox.matlin

import kotlin.math.sqrt

/**
 * A generic Bézier curve. Either [Linear], [Quadratic] or [Cubic]. Bézier curves are parametric curves with individual
 * definitions for X and Y. These definitions are polynomials of the variable `t`.
 */
sealed interface Bezier {
    /**
     * The X coordinate at the start of the curve. This is the X coordinate at `t = 0`.
     */
    var sx: Double

    /**
     * The Y coordinate at the start of the curve. This is the Y coordinate at `t = 0`.
     */
    var sy: Double

    /**
     * The X coordinate at the end of the curve. This is the X coordinate at `t = 1`.
     */
    var ex: Double

    /**
     * The Y coordinate at the end of the curve. This is the Y coordinate at `t = 1`.
     */
    var ey: Double

    /**
     * The constant coefficient of the polynomial that represents the X coordinate of this curve:
     * `x = x0 + x1*t + x2*t*t + x3*t*t*t`.
     */
    val x0: Double

    /**
     * The linear coefficient of the polynomial that represents the X coordinate of this curve:
     * `x = x0 + x1*t + x2*t*t + x3*t*t*t`.
     */
    val x1: Double

    /**
     * The quadratic coefficient of the polynomial that represents the X coordinate of this curve:
     * `x = x0 + x1*t + x2*t*t + x3*t*t*t`.
     *
     * This is 0 for [Linear].
     */
    val x2: Double

    /**
     * The cubic coefficient of the polynomial that represents the X coordinate of this curve:
     * `x = x0 + x1*t + x2*t*t + x3*t*t*t`.
     *
     * This is 0 for [Linear] and [Quadratic].
     */
    val x3: Double

    /**
     * The constant coefficient of the polynomial that represents the Y coordinate of this curve:
     * `y = y0 + y1*t + y2*t*t + y3*t*t*t`.
     */
    val y0: Double

    /**
     * The linear coefficient of the polynomial that represents the Y coordinate of this curve:
     * `y = y0 + y1*t + y2*t*t + y3*t*t*t`.
     */
    val y1: Double

    /**
     * The quadratic coefficient of the polynomial that represents the Y coordinate of this curve:
     * `y = y0 + y1*t + y2*t*t + y3*t*t*t`.
     *
     * This is 0 for [Linear].
     */
    val y2: Double

    /**
     * The cubic coefficient of the polynomial that represents the Y coordinate of this curve:
     * `y = y0 + y1*t + y2*t*t + y3*t*t*t`.
     *
     * This is 0 for [Linear] and [Quadratic].
     */
    val y3: Double

    /**
     * Returns whether this curve can be a quadratic Bézier curve. This returns true if and only if both [x3] and [y3]
     * are 0 - that is, both parameters of the curve are quadratic polynomials.
     */
    val isQuadratic get() = x3 == 0.0 && y3 == 0.0

    /**
     * Returns whether this curve can be a linear Bézier curve. This returns true if and only if all coefficients
     * [x3], [y3], [x2] and [y2] are 0 - that is, both parameters of the curve are linear polynomials.
     */
    val isLinear get() = isQuadratic && x2 == 0.0 && y2 == 0.0

    /**
     * Samples an X coordinate of the curve, at `t`. `t` is typically a value between 0 and 1 where 0 samples [sx] and
     * 1 samples [ex].
     */
    fun x(t: Double): Double

    /**
     * Samples an Y coordinate of the curve, at `t`. `t` is typically a value between 0 and 1 where 0 samples [sy] and
     * 1 samples [ey].
     */
    fun y(t: Double): Double

    /**
     * Samples the X coordinate of the derivative of the curve, at `t`. `t` is typically a value between 0 and 1 where 0
     * samples the derivative at [sx] and 1 samples the derivative at [ex].
     */
    fun dx(t: Double): Double

    /**
     * Samples the Y coordinate of the derivative of the curve, at `t`. `t` is typically a value between 0 and 1 where 0
     * samples the derivative at [sy] and 1 samples the derivative at [ey].
     */
    fun dy(t: Double): Double

    /**
     * Samples the X coordinate of the second derivative of the curve, at `t`. `t` is typically a value between 0 and 1
     * where 0 samples the second derivative at [sx] and 1 samples the second derivative at [ex].
     */
    fun ddx(t: Double): Double

    /**
     * Samples the Y coordinate of the second derivative of the curve, at `t`. `t` is typically a value between 0 and 1
     * where 0 samples the second derivative at [sy] and 1 samples the second derivative at [ey].
     */
    fun ddy(t: Double): Double

    /**
     * Finds the bounding box of this curve. This is the smallest axis-aligned rectangle that perfectly contains the
     * curve. It will mutate the given [BoundingBox] instance (which is a new instance by default), and then return it.
     */
    fun boundingBox(out: BoundingBox = BoundingBox()): BoundingBox

    /**
     * Constructs a new curve instance representing the same curve.
     */
    fun duplicate(): Bezier

    /**
     * Constructs a curve equivalent to this curve, but in the opposite direction. That is, sampling the reversed curve
     * at `t` will be equal to sampling the non-reversed curve at `1 - t`. This method mutates this curve to become the
     * reversed curve.
     */
    fun reverse()

    /**
     * Constructs a curve of the same order which represents the segment of this curve from 0 to `t`. It then mutates
     * this curve to become the constructed curve.
     */
    fun cutStart(t: Double)

    /**
     * Constructs a curve of the same order which represents the segment of this curve from `t` to 1. It then mutates
     * this curve to become the constructed curve.
     */
    fun cutEnd(t: Double)

    /**
     * Constructs a curve of the same order which represents the segment of this curve from `t1` to `t2`. It then mutates
     * this curve to become the constructed curve.
     */
    fun cutPart(t1: Double, t2: Double) {
        if (t1 == 0.0) {
            cutStart(t2)
        } else if (t2 == 0.0) {
            cutStart(t1)
            reverse()
        } else if (t2 < t1) {
            cutStart(t1)
            cutEnd(unlerp(0.0, t1, t2))
            reverse()
        } else {
            cutStart(t2)
            cutEnd(unlerp(0.0, t2, t1))
        }
    }

    /**
     * Calculates a measure of how non-linear this curve is. A perfect linear curve has a nonlinearity factor of 0, and
     * anything off that makes this value nonzero. Note that this means the curve must not only be a straight line, its
     * derivative must be the derivative of a straight line as well (i.e. it should be constant).
     *
     * A Bézier curve of degree `N` is linear if its control points are on one line, and are evenly spaced. So what
     * it computes is the distance from each control point to the point where that control point should be to make the
     * curve linear. It then takes the sum of all these distances and divides it by `N` (the degree of the curve). So:
     * - A linear curve has no control points. It computes an empty sum divided by 1, which is 0, as per definition.
     * - A quadratic curve has one control point, so it computes the distance of that point to the center of the line
     *   between its endpoints, and divides it by 2.
     * - A cubic curve has two control points, so it computes the distance of the point A to 1/3rd along the line, plus
     *   the the distance of the point B to 2/3rds along the line, and then divides that by 3.
     *
     * A nice property of this nonlinearity is that it scales with the curve: scaling up a curve scales its nonlinearity
     * coefficient equally. This allows for approximating functions to set thresholds on when they will assume the curve
     * to be a straight line they can perform a simple approximation on.
     */
    fun nonlinearity(): Double

    fun nonquadraticity(): Double {
        return sqrt(x3*x3+y3*y3)
    }
}
