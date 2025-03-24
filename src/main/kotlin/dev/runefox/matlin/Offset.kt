package dev.runefox.matlin

import kotlin.math.*

internal fun Linear.linearOffset(offset: Double): Linear {
    val l = linearLength()
    val nx = -(ey - sy) / l
    val ny = +(ex - sx) / l

    return Linear(
        sx + nx * offset,
        sy + ny * offset,
        ex + nx * offset,
        ey + ny * offset
    )
}

fun Linear.offset(offset: Double): MutableList<Bezier> {
    return mutableListOf(linearOffset(offset))
}

private inline fun Bezier.sampleOffset(t: Double, offset: Double, res: (Double, Double) -> Unit) {
    val x = x(t)
    val y = y(t)
    val dx = dx(t)
    val dy = dy(t)
    val dl = sqrt(dx * dx + dy * dy)

    val nx = -dy / dl
    val ny = dx / dl

    res(x + nx * offset, y + ny * offset)
}

fun Quadratic.offset(offset: Double): MutableList<Bezier> {
    var x1 = 0.0
    var y1 = 0.0
    var x2 = 0.0
    var y2 = 0.0
    var x3 = 0.0
    var y3 = 0.0

    sampleOffset(0.0, offset) { x, y -> x1 = x; y1 = y }
    sampleOffset(0.5, offset) { x, y -> x2 = x; y2 = y }
    sampleOffset(1.0, offset) { x, y -> x3 = x; y3 = y }

    val q = Quadratic()
    q.through3Points(x1, y1, 0.0, x2, y2, 0.5, x3, y3, 1.0)

    return mutableListOf(q)
}

fun Cubic.offset(offset: Double): MutableList<Bezier> {
    var x1 = 0.0
    var y1 = 0.0
    var x2 = 0.0
    var y2 = 0.0
    var x3 = 0.0
    var y3 = 0.0
    var x4 = 0.0
    var y4 = 0.0

    sampleOffset(0.0, offset) { x, y -> x1 = x; y1 = y }
    sampleOffset(1/3.0, offset) { x, y -> x2 = x; y2 = y }
    sampleOffset(2/3.0, offset) { x, y -> x3 = x; y3 = y }
    sampleOffset(1.0, offset) { x, y -> x4 = x; y4 = y }

    val q = Cubic()
    q.through4Points(x1, y1, 0.0, x2, y2, 1/3.0, x3, y3, 2/3.0, x4, y4, 1.0)

    return mutableListOf(q)
}

@Suppress("UNCHECKED_CAST")
fun Bezier.offset(offset: Double): MutableList<Bezier> {
    return when (this) {
        is Linear -> offset(offset)
        is Quadratic -> offset(offset)
        is Cubic -> offset(offset)
    }
}

private class OutputBuilder(
    var output: MutableList<Cubic>,
    val scale: Double,
    val translationX: Double,
    val translationY: Double
) {
    var prevX = 0.0
    var prevY = 0.0
    var prevXT = 0.0
    var prevYT = 0.0
    var cuspX = 0.0
    var cuspY = 0.0
    var needCuspArc = false
    var cuspArcCw = false

    fun moveTo(x: Double, y: Double) {
        prevX = x
        prevY = y
        prevXT = x * scale + translationX
        prevYT = y * scale + translationY
    }

    fun MutableList<Cubic>.addLine(sx: Double, sy: Double, ex: Double, ey: Double) {
        val cubic = Cubic()
        cubic.setXPolynomial(0.0, 0.0, ex - sx, sx)
        cubic.setYPolynomial(0.0, 0.0, ey - sy, sy)
        this += cubic
    }

    fun MutableList<Cubic>.addCubic(
        sx: Double,
        sy: Double,
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        ex: Double,
        ey: Double
    ) {
        this += Cubic(sx, sy, ax, ay, bx, by, ex, ey)
    }

    fun lineTo(x: Double, y: Double) {
        val px = prevX
        val py = prevY

        if (x != px || y != py) {
            val tx = x * scale + translationX
            val ty = y * scale + translationY

            output.addLine(prevXT, prevYT, tx, ty)

            prevX = x
            prevY = y
            prevXT = tx
            prevYT = ty
        }
    }

    fun cubicTo(ax: Double, ay: Double, bx: Double, by: Double, x: Double, y: Double) {
        val px = prevX
        val py = prevY

        if (!(px == ax && py == ay) || !(px == bx && py == by) || !(px == x && py == y)) {
            val tax = ax * scale + translationX
            val tay = ay * scale + translationY
            val tbx = bx * scale + translationX
            val tby = by * scale + translationY
            val tx = x * scale + translationX
            val ty = y * scale + translationY

            output.addCubic(prevXT, prevYT, tax, tay, tbx, tby, tx, ty)
        }
    }

    fun arcTo(cx: Double, cy: Double, x: Double, y: Double, clockwise: Boolean) {
        val arcFromX = prevX
        val arcFromY = prevY

        val arcRadius = sqrt((cx - arcFromX) * (cx - arcFromX) + (cy - arcFromY) * (cy - arcFromY))
        if (arcRadius < 1e-8)
            return

        fun Linear.angle(): Double {
            val dx = ex - sx
            val dy = ey - sy
            val theta = Math.atan2(-dy, dx)
            val thetaNormalized = if (theta < 0) theta + 360 else theta

            if (abs(thetaNormalized - 360.0) < 1e-8) {
                return 0.0
            }

            if (abs(thetaNormalized) < 1e-8) {
                return 0.0
            }

            return thetaNormalized
        }

        fun Linear.isRoughlyPoint() = abs(ex - sx) < 1e-8 && abs(ey - sy) < 1e-8

        fun Linear.angleRelativeTo(l: Linear): Double {
            if (isRoughlyPoint() || l.isRoughlyPoint()) {
                return 0.0
            }

            val c = ((ex - sx) * (l.ex - l.sx) + (ey - sy) * (l.ey - l.sy)) / (length() * l.length());

            val epsilon = 8e-8;

            if (c >= (-1 - epsilon) && c <= (1 + epsilon)) {
                return acos(clamp(-1.0, 1.0, c))
            }

            return 0.0
        }

        val centerToCurrentPoint = Linear(cx, cy, arcFromX, arcFromY)
        val centerToEndPoint = Linear(cx, cy, x, y)
        val startAngle = centerToCurrentPoint.angle()

        var sweepAngle = centerToCurrentPoint.angleRelativeTo(centerToEndPoint)

        if (abs(sweepAngle) < 1e-8)
            return

        val orientation = turn(cx, cy, arcFromX, arcFromY, x, y)
        if (abs(orientation) > 1e-8) {
            val detClockwise = orientation > 0
            if (detClockwise != clockwise)
                sweepAngle = (2 * PI) - sweepAngle
        }

        val nSteps = ceil(sweepAngle / (2 * PI)).toInt()
        val step = sweepAngle / nSteps * (if (clockwise) -1 else 1)

        var s = -sin(startAngle)
        var c = cos(startAngle)

        for (i in 1..nSteps) {
            val a1 = startAngle + step * i

            val s1 = -sin(a1)
            val c1 = cos(a1)

            val unitCurve = findUnitCubicCurveForArc(c, s, c1, s1)

            val px1 = unitCurve.x1 * arcRadius + cx
            val py1 = unitCurve.y1 * arcRadius + cy
            val px2 = unitCurve.x2 * arcRadius + cx
            val py2 = unitCurve.y2 * arcRadius + cy

            if (i < nSteps) {
                val px3 = unitCurve.x3 * arcRadius + cx
                val py3 = unitCurve.y3 * arcRadius + cy
                cubicTo(px1, py1, px2, py2, px3, py3)
            } else {
                cubicTo(px1, py1, px2, py2, x, y)
            }

            s = s1
            c = c1
        }
    }

    fun maybeAddCuspArc(x: Double, y: Double) {
        if (needCuspArc) {
            needCuspArc = false

            arcTo(cuspX, cuspY, x, y, cuspArcCw)
            cuspX = 0.0
            cuspY = 0.0
            cuspArcCw = false
        }
    }
}

internal fun turn(x0: Double, y0: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double {
    val x01 = x1 - x0
    val y01 = y1 - y0
    val x02 = x2 - x0
    val y02 = y2 - y0
    return x01 * y02 - x02 * y01
}

internal fun findUnitCubicCurveForArc(sx: Double, sy: Double, ex: Double, ey: Double): Cubic {
    val q1 = sx * sx + sy * sy
    val q2 = q1 + sx * ex + sy * ey
    val k2 = (4.0 / 3.0) * (sqrt(2 * q1 * q2) - q2) / (sx * ey - sy * ex)
    val ax = sx - k2 * sy
    val ay = sy + k2 * sx
    val bx = ex + k2 * ey
    val by = ey - k2 * ex

    return Cubic(sx, sy, ax, ay, bx, by, ex, ey)
}


internal inline fun Cubic.curvatureExtrema(result: (Double) -> Unit): Int {
    val axx = ax - sx
    val bxx = bx - 2 * ax + sx
    val cxx = ex + 3 * (ax - bx) - sx

    val cox0 = cxx * cxx
    val cox1 = 3 * bxx * cxx
    val cox2 = 2 * bxx * bxx + cxx * axx
    val cox3 = axx * bxx

    val ayy = ay - sy
    val byy = by - 2 * ay + sy
    val cyy = ey + 3 * (ay - by) - sy

    val coy0 = cyy * cyy
    val coy1 = 3 * byy * cyy
    val coy2 = 2 * byy * byy + cyy * ayy
    val coy3 = ayy * byy

    return solveCubic(cox0 + coy0, cox1 + coy1, cox2 + coy2, cox3 + coy3) {
        result(it)
    }
}
