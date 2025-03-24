package dev.runefox.matlin

@Suppress("ConvertTwoComparisonsToRangeCheck")
inline fun Cubic.inflections(all: Boolean = false, result: (Double) -> Unit): Int {
    // https://stackoverflow.com/a/35906870/5555016
    val a = 3 * (x2 * y3 - y2 * x3)
    val b = 3 * (x1 * y3 - y1 * x3)
    val c = x1 * y2 - y1 * x2
    var res = 0
    solveQuadratic(a, b, c) {
        if (all || (it >= 0 && it <= 1)) {
            res += 1
            result(it)
        }
    }
    return res
}

inline fun Bezier.inflections(all: Boolean = false, result: (Double) -> Unit): Int {
    return when (this) {
        is Cubic -> inflections(all, result)

        // Quadratic and linear curves have no inflection points
        is Quadratic -> 0
        is Linear -> 0
    }
}

fun Cubic.cutAtInflections(out: MutableList<Cubic> = mutableListOf()): MutableList<Cubic> {
    out.clear()

    val ts = DoubleArray(2)
    var n = 0
    inflections { ts[n++] = it }

    ts.sort(0, n)

    if (n < 1) {
        out += copy()
        return out
    }

    val sx = sx
    val sy = sy
    val ax = ax
    val ay = ay
    val bx = bx
    val by = by
    val ex = ex
    val ey = ey

    if (n < 2) {
        cutStart(ts[0])
        out += copy()
        set(sx, sy, ax, ay, bx, by, ex, ey)

        cutEnd(ts[0])
        out += copy()
        set(sx, sy, ax, ay, bx, by, ex, ey)
    } else {
        cutStart(ts[0])
        out += copy()
        set(sx, sy, ax, ay, bx, by, ex, ey)

        cutPart(ts[0], ts[1])
        out += copy()
        set(sx, sy, ax, ay, bx, by, ex, ey)

        cutEnd(ts[1])
        out += copy()
        set(sx, sy, ax, ay, bx, by, ex, ey)
    }

    return out
}

fun Quadratic.cutAtInflections(out: MutableList<Quadratic> = mutableListOf()): MutableList<Quadratic> {
    out.clear()
    out += copy()
    return out
}

fun Linear.cutAtInflections(out: MutableList<Linear> = mutableListOf()): MutableList<Linear> {
    out.clear()
    out += copy()
    return out
}

@Suppress("UNCHECKED_CAST")
fun Bezier.cutAtInflections(out: MutableList<Bezier> = mutableListOf()): MutableList<Bezier> {
    return when(this) {
        is Linear -> cutAtInflections(out as MutableList<Linear>) as MutableList<Bezier>
        is Quadratic -> cutAtInflections(out as MutableList<Quadratic>) as MutableList<Bezier>
        is Cubic -> cutAtInflections(out as MutableList<Cubic>) as MutableList<Bezier>
    }
}
