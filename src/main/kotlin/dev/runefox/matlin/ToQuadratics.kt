package dev.runefox.matlin

fun Linear.toQuadraticSpline(out: MutableList<Quadratic> = mutableListOf()): MutableList<Quadratic> {
    val q = Quadratic()
    q.setLinear(this)
    out += q
    return out
}

fun Quadratic.toQuadraticSpline(out: MutableList<Quadratic> = mutableListOf()): MutableList<Quadratic> {
    out += copy()
    return out
}

private fun Cubic.toQuadratic(): Quadratic? {
    val q = Quadratic()
    q.set(
        sx = sx,
        sy = sy,
        ax = (ex - sx) / 2 + sx,
        ay = (ey - sy) / 2 + sy,
        ex = ex,
        ey = ey
    )

    intersectLines(
        sx, sy,
        ax, ay,
        ex, ey,
        bx, by
    ) { t, u ->
        if (t <= 0 || u <= 0)
            return null

        q.ax = lerp(sx, ax, t)
        q.ay = lerp(sy, ay, t)
    }

    return q
}

fun Cubic.toQuadraticSpline(
    out: MutableList<Quadratic> = mutableListOf(),
    nonquadraticityThreshold: Double = 0.2
): MutableList<Quadratic> {
    val nonquadraticity = nonquadraticity()
    if (nonquadraticity < nonquadraticityThreshold) {
        val q =  toQuadratic()
        if (q != null) {
            out += q
            return out
        }
    }

    val (sx, sy, ax, ay, bx, by, ex, ey) = this

    cutStart(0.5)
    toQuadraticSpline(out, nonquadraticityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    cutEnd(0.5)
    toQuadraticSpline(out, nonquadraticityThreshold)
    set(sx, sy, ax, ay, bx, by, ex, ey)

    return out
}

fun Bezier.toQuadraticSpline(
    out: MutableList<Quadratic> = mutableListOf(),
    nonquadraticityThreshold: Double = 0.2
): MutableList<Quadratic> {
    return when (this) {
        is Linear -> toQuadraticSpline(out)
        is Quadratic -> toQuadraticSpline(out)
        is Cubic -> toQuadraticSpline(out, nonquadraticityThreshold)
    }
}
