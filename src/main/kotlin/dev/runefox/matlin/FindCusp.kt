package dev.runefox.matlin

import kotlin.math.abs

// Cusps exist at any t such that 0 < t < 1 and dx(t) = dy(t) = 0
// Since this is just a matter of finding out if the derivative goes through the origin,
// this can be done algebraically.

inline fun Quadratic.findCusps(accuracy: Double = 1e-1, result: (Double) -> Unit): Int {
    val dx1 = 2*x2
    val dx0 = x1

    val dy1 = 2*y2
    val dy0 = y1

    var xt = -1.0
    var xx = -1.0
    var xy = -1.0
    solveLinear(dx1, dx0) { t ->
        xx = dx(t)
        xy = dy(t)
        xt = t
    }

    if (abs(xy) < accuracy && abs(xx) < accuracy && xt >= 0 && xt <= 1) {
        result(xt)
        return 1
    }

    var yt = -1.0
    var yx = -1.0
    var yy = -1.0
    solveLinear(dy1, dy0) { t ->
        yx = dx(t)
        yy = dy(t)
        yt = t
    }


    if (abs(yx) < accuracy && abs(yy) < accuracy && yt >= 0 && yt <= 1) {
        result(yt)
        return 1
    }

    return 0
}

inline fun Cubic.findCusps(accuracy: Double = 1e-1, result: (Double) -> Unit): Int {
    var res = 0

    val dx2 = 3*x3
    val dx1 = 2*x2
    val dx0 = x1

    val dy2 = 3*y3
    val dy1 = 2*y2
    val dy0 = y1

    var xn = 0
    var xt1 = -1.0
    var xx1 = -1.0
    var xy1 = -1.0
    var xt2 = -1.0
    var xx2 = -1.0
    var xy2 = -1.0
    var xf1 = -1.0
    var xf2 = -1.0
    solveQuadratic(dx2, dx1, dx0) { t ->
        if (xn == 0) {
            xx1 = dx(t)
            xy1 = dy(t)
            xt1 = t
        } else {
            xx2 = dx(t)
            xy2 = dy(t)
            xt2 = t
        }
        xn ++
    }

    if (abs(xy1) < accuracy && abs(xx1) < accuracy && xt1 >= 0 && xt1 <= 1) {
        result(xt1)
        res ++
        xf1 = xt1
    }

    if (abs(xy2) < accuracy && abs(xx2) < accuracy && xt2 >= 0 && xt2 <= 1) {
        result(xt2)
        res ++
        xf2 = xt2
    }

    if (res == 2)
        return 2

    var yn = 0
    var yt1 = -1.0
    var yx1 = -1.0
    var yy1 = -1.0
    var yt2 = -1.0
    var yx2 = -1.0
    var yy2 = -1.0
    solveQuadratic(dx2, dy1, dy0) { t ->
        if (yn == 0) {
            yx1 = dx(t)
            yy1 = dy(t)
            yt1 = t
        } else {
            yx2 = dx(t)
            yy2 = dy(t)
            yt2 = t
        }
        yn ++
    }

    if (abs(yy1) < accuracy && abs(yx1) < accuracy && yt1 >= 0 && yt1 <= 1 && abs(yt1 - xf1) * 16 > accuracy  && abs(yt1 - xf2) * 16 > accuracy) {
        result(yt1)
        res ++
    }

    if (res == 2)
        return 2

    if (abs(yy2) < accuracy && abs(yx2) < accuracy && yt2 >= 0 && yt2 <= 1 && abs(yt2 - xf1) * 16 > accuracy  && abs(yt2 - xf2) * 16 > accuracy) {
        result(yt2)
        res ++
    }

    return res
}

inline fun Bezier.findCusps(accuracy: Double = 1e-1, result: (Double) -> Unit): Int {
    return when(this) {
        is Linear -> 0
        is Quadratic -> findCusps(accuracy, result)
        is Cubic -> findCusps(accuracy, result)
    }
}
