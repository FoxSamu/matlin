package dev.runefox.matlin

/**
 * Cuts this curve at its extrema. This function simply adds a copy of this [Linear] curve to the output, as a line does
 * not have any extrema except its two endpoints.
 */
fun Linear.cutAtExtrema(out: MutableList<Linear> = mutableListOf()): MutableList<Linear> {
    out.clear()
    out += copy()
    return out
}

/**
 * Cuts this curve at its extrema. Extrema are where the curve's derivative is either horizontal or vertical. This cuts
 * the curve in such a way that each subdivision has a bounding box with the start and end of the curve hitting the
 * corner of the bounding box. A [Quadratic] curve will be cut at at most 2 points, i.e it will have at most 3
 * subdivisions.
 */
fun Quadratic.cutAtExtrema(out: MutableList<Quadratic> = mutableListOf()): MutableList<Quadratic> {
    out.clear()
    val dx = ax * 2
    val dy = ay * 2

    var t1 = (-dx + 2 * sx) / (2 * (ex + sx - dx))
    var t2 = (-dy + 2 * sy) / (2 * (ey + sy - dy))

    if (t1 > t2) {
        val t = t1
        t1 = t2
        t2 = t
    }

    val r = Quadratic()
    r.set(this)
    var rt = 0.0

    if (t1 > 0 && t1 < 1) {
        val q = Quadratic()
        r.cut(t1, q, r)
        out += q
        rt = t1
    }

    if (t2 > rt && t2 < 1) {
        val q = Quadratic()
        r.cut(unlerp(rt, 1.0, t2), q, r)
        out += q
    }

    out += r
    return out
}

/**
 * Cuts this curve at its extrema. Extrema are where the curve's derivative is either horizontal or vertical. This cuts
 * the curve in such a way that each subdivision has a bounding box with the start and end of the curve hitting the
 * corner of the bounding box. A [Cubic] curve will be cut at at most 4 points, i.e it will have at most 5
 * subdivisions.
 *
 * Cutting the curve at its extrema often makes it easier to compute with. For example, these curves will never
 * self-intersect, so they can be used to compute self-intersection
 */
fun Cubic.cutAtExtrema(out: MutableList<Cubic> = mutableListOf()): MutableList<Cubic> {
    out.clear()
    val arr = DoubleArray(4)
    var i = 0

    // First find extrema
    val tbax = tbax
    val tasx = tasx
    solveQuadratic(3*(ex-sx-tbax), 2*(tbax-tasx), tasx) { pt ->
        if (pt > 0 && pt < 1) {
            arr[i++] = pt
        }
    }

    val tbay = tbay
    val tasy = tasy
    solveQuadratic(3*(ey-sy-tbay), 2*(tbay-tasy), tasy) { pt ->
        if (pt > 0 && pt < 1) {
            arr[i++] = pt
        }
    }

    // Now reorder them so that they're in increasing order of t
    arr.sort(0, i)

    val r = Cubic()
    r.set(this)
    var rt = 0.0

    for (j in 0..<i) {
        val t = arr[j]
        val c = Cubic()
        r.cut(unlerp(rt, 1.0, t), c, r)
        out += c
        rt = t
    }

    out += r

    return out
}
