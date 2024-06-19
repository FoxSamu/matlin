package dev.runefox.matlin

inline fun Linear.intersectHoriz(y: Double, result: (Double) -> Unit): Int {
    return solveLinear(y1, y0 - y, result)
}

inline fun Quadratic.intersectHoriz(y: Double, result: (Double) -> Unit): Int {
    return solveQuadratic(y2, y1, y0 - y, result)
}

inline fun Cubic.intersectHoriz(y: Double, result: (Double) -> Unit): Int {
    return solveCubic(y3, y2, y1, y0 - y, result)
}

inline fun Bezier.intersectHoriz(y: Double, result: (Double) -> Unit): Int {
    return when (this) {
        is Linear -> intersectHoriz(y, result)
        is Quadratic -> intersectHoriz(y, result)
        is Cubic -> intersectHoriz(y, result)
    }
}

inline fun Linear.intersectVert(x: Double, result: (Double) -> Unit): Int {
    return solveLinear(x1, x0 - x, result)
}

inline fun Quadratic.intersectVert(x: Double, result: (Double) -> Unit): Int {
    return solveQuadratic(x2, x1, x0 - x, result)
}

inline fun Cubic.intersectVert(x: Double, result: (Double) -> Unit): Int {
    return solveCubic(x3, x2, x1, x0 - x, result)
}

inline fun Bezier.intersectVert(x: Double, result: (Double) -> Unit): Int {
    return when (this) {
        is Linear -> intersectVert(x, result)
        is Quadratic -> intersectVert(x, result)
        is Cubic -> intersectVert(x, result)
    }
}
