package dev.runefox.matlin.geotest

import dev.runefox.matlin.*
import net.shadew.geotest.*
import org.joml.Vector2d
import org.lwjgl.glfw.GLFW
import kotlin.math.*


abstract class BezierScene : DraggablePointsScene(), KeyDown {
    abstract val bezier: Bezier

    private val bb = BoundingBox()
    private val grid = Grid()

    var gridVisible = true

    private val t1 = add(Point("t1", MAGENTA + SOLID, Vector2d(0.25, 1.0), false))
    private val t2 = add(Point("t2", MAGENTA + SOLID, Vector2d(0.75, 2.0), false))
    private val l = add(Point("l", MAGENTA + SOLID, Vector2d(2.0, 1.0), false))
    private val s = add(Point("s", MAGENTA + SOLID, Vector2d(0.0, 0.0), false))
    private val p = add(Point("p", GREEN + SOLID, Vector2d(-3.0, -1.0), false))
    private val q = add(Point("q", GREEN + SOLID, Vector2d(3.0, 1.0), false))

    private val qs = add(Point("S", CYAN + SOLID, Vector2d(-3.0, 1.0), false))
    private val qa = add(Point("A", CYAN + SOLID, Vector2d(-2.0, -3.0), false))
    private val qe = add(Point("E", CYAN + SOLID, Vector2d(3.0, 1.0), false))

    private val cs = add(Point("S", CYAN + SOLID, Vector2d(-3.0, 1.0), false))
    private val ca = add(Point("A", CYAN + SOLID, Vector2d(-1.0, -2.0), false))
    private val cb = add(Point("B", CYAN + SOLID, Vector2d(1.0, 2.0), false))
    private val ce = add(Point("E", CYAN + SOLID, Vector2d(3.0, -1.0), false))

    private val c = add(Point("C", CYAN + SOLID, Vector2d(0.0, -1.0), false))
    private val r = add(Point("R", CYAN + SOLID, Vector2d(1.5, 0.0), false))

    var mode = BezierTest.none

    abstract fun updateBezier(ctx: GeometryContext)
    abstract fun drawActualBezier(ctx: GeometryContext)
    abstract fun drawControlLines(ctx: GeometryContext)
    abstract fun drawControlPoints(ctx: GeometryContext, out: (Double, Double) -> Unit)

    private val lin = Linear()
    private val quad = Quadratic()
    private val cub = Cubic()

    override fun keyDown(ctx: GeometryContext, key: Int, mods: Int, repeat: Boolean) {
        if (key == GLFW.GLFW_KEY_G && !repeat)
            gridVisible = !gridVisible
    }

    private fun displayError(ctx: GeometryContext, error: String) {
        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
        ctx.drawHudTextBg(error, ctx.windowW() / 2, ctx.windowH() / 2, TRANSPARENT, 20f, 5f, 3f)
        ctx.drawHudText(error, ctx.windowW() / 2, ctx.windowH() / 2, RED + SOLID, 20f)
    }

    private fun drawLin(ctx: GeometryContext, col: Int, lin: Linear = this.lin) {
        ctx.begin()
        ctx.moveTo(lin.sx.toFloat(), lin.sy.toFloat())
        ctx.lineTo(lin.ex.toFloat(), lin.ey.toFloat())
        ctx.stroke(2f, col + SOLID)

        ctx.drawPointCircle(lin.sx.toFloat(), lin.sy.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(lin.ex.toFloat(), lin.ey.toFloat(), col + SOLID, 6f)
    }

    private fun drawQuad(ctx: GeometryContext, col: Int, quad: Quadratic = this.quad) {
        ctx.begin()
        ctx.moveTo(quad.sx.toFloat(), quad.sy.toFloat())
        ctx.quadTo(quad.ax.toFloat(), quad.ay.toFloat(), quad.ex.toFloat(), quad.ey.toFloat())
        ctx.stroke(2f, col + SOLID)

        ctx.begin()
        ctx.moveTo(quad.sx.toFloat(), quad.sy.toFloat())
        ctx.lineTo(quad.ax.toFloat(), quad.ay.toFloat())
        ctx.lineTo(quad.ex.toFloat(), quad.ey.toFloat())
        ctx.stroke(2f, col + TRANSPARENT)

        ctx.drawPointCircle(quad.sx.toFloat(), quad.sy.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(quad.ax.toFloat(), quad.ay.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(quad.ex.toFloat(), quad.ey.toFloat(), col + SOLID, 6f)
    }

    private fun drawCub(ctx: GeometryContext, col: Int, cub: Cubic = this.cub) {
        ctx.begin()
        ctx.moveTo(cub.sx.toFloat(), cub.sy.toFloat())
        ctx.cubicTo(
            cub.ax.toFloat(),
            cub.ay.toFloat(),
            cub.bx.toFloat(),
            cub.by.toFloat(),
            cub.ex.toFloat(),
            cub.ey.toFloat()
        )
        ctx.stroke(2f, col + SOLID)

        ctx.begin()
        ctx.moveTo(cub.sx.toFloat(), cub.sy.toFloat())
        ctx.lineTo(cub.ax.toFloat(), cub.ay.toFloat())
        ctx.lineTo(cub.bx.toFloat(), cub.by.toFloat())
        ctx.lineTo(cub.ex.toFloat(), cub.ey.toFloat())
        ctx.stroke(2f, col + TRANSPARENT)

        ctx.drawPointCircle(cub.sx.toFloat(), cub.sy.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(cub.ax.toFloat(), cub.ay.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(cub.bx.toFloat(), cub.by.toFloat(), col + SOLID, 6f)
        ctx.drawPointCircle(cub.ex.toFloat(), cub.ey.toFloat(), col + SOLID, 6f)
    }


    fun drawXPolynomial(a: Double, b: Double, c: Double, d: Double, col: Int, ctx: GeometryContext) {
        val density = 5
        val wdt = (ctx.windowW() / density).toInt() + 1
        ctx.begin()
        for (i in 0..wdt) {
            val sx = i * density
            val x = ctx.posXToWorld(sx.toFloat()).toDouble()
            val y = ((((a * x + b) * x) + c) * x + d)

            if (i == 0) {
                ctx.moveTo(x.toFloat(), y.toFloat())
            } else {
                ctx.lineTo(x.toFloat(), y.toFloat())
            }
        }
        ctx.stroke(2f, col)
    }

    fun drawYPolynomial(a: Double, b: Double, c: Double, d: Double, col: Int, ctx: GeometryContext) {
        val density = 5
        val hgt = (ctx.windowH() / density).toInt() + 1
        ctx.begin()
        for (i in 0..hgt) {
            val sy = i * density
            val y = ctx.posYToWorld(sy.toFloat()).toDouble()
            val x = ((((a * y + b) * y) + c) * y + d)

            if (i == 0) {
                ctx.moveTo(x.toFloat(), y.toFloat())
            } else {
                ctx.lineTo(x.toFloat(), y.toFloat())
            }
        }
        ctx.stroke(2f, col)
    }

    fun drawSpline(ctx: GeometryContext, col: Int, spline: MutableList<in Bezier>) {
        for (curve in spline) {
            when (curve) {
                is Linear -> drawLin(ctx, col, curve)
                is Quadratic -> drawQuad(ctx, col, curve)
                is Cubic -> drawCub(ctx, col, curve)
            }
        }
    }


    fun circIsc(ctx: GeometryContext, t: Double, x: Double, y: Double, angle: Double, c: Vector2d, r: Double) {
        ctx.drawPointCircle(
            x.toFloat(),
            y.toFloat(),
            WHITE + SOLID,
            10f
        )

        val sin = sin(angle) * r
        val cos = cos(angle) * r

        ctx.drawSegment(c.x.toFloat(), c.y.toFloat(), (c.x + cos).toFloat(), (c.y + sin).toFloat(), RED + TRANSPARENT, 2f)

        var deg = angle/ PI *180
        if (deg > 359.951f)
            deg = 0.0

        val text = "%.1f°".format(deg)
        ctx.drawTextBg(text, (c.x + cos/2).toFloat(), (c.y + sin/2).toFloat(), 0f, 0f, TRANSPARENT, 12f, 4f, 2f)
        ctx.drawText(text, (c.x + cos/2).toFloat(), (c.y + sin/2).toFloat(), 0f, 0f, RED + SOLID, 12f)

        val text2 = "%.3f".format(t)
        ctx.drawTextBg(text2, (c.x + cos).toFloat(), (c.y + sin).toFloat(), 0f, 15f, TRANSPARENT, 12f, 4f, 2f)
        ctx.drawText(text2, (c.x + cos).toFloat(), (c.y + sin).toFloat(), 0f, 15f, WHITE + SOLID, 12f)
    }

    override fun init(ctx: GeometryContext) {
        super.init(ctx)
        ctx.onKeyDown(this)
    }

    override fun stop(ctx: GeometryContext) {
        super.stop(ctx)
        ctx.removeKeyDown(this)
    }

    override fun drawScene(ctx: GeometryContext) {
        updateBezier(ctx)

        t1.vec.y = 1.0
        l.vec.y = 1.0
        t2.vec.y = 2.0

        if (gridVisible)
            grid.draw(ctx)

        ctx.begin()
        drawActualBezier(ctx)
        when (mode) {
            BezierTest.evaluation, BezierTest.cut, BezierTest.to_cubic,
            BezierTest.cut_at_extrema
            -> ctx.stroke(4f, TRANSPARENT + WHITE)
            BezierTest.intersect_cubic, BezierTest.intersect_quadratic
            -> ctx.stroke(2f, SOLID + YELLOW)
            else -> ctx.stroke(2f, SOLID + MAGENTA)
        }

        t1.enabled = false
        t2.enabled = false
        s.enabled = false
        p.enabled = false
        q.enabled = false
        qs.enabled = false
        qa.enabled = false
        qe.enabled = false
        cs.enabled = false
        ca.enabled = false
        cb.enabled = false
        ce.enabled = false
        c.enabled = false
        r.enabled = false
        l.enabled = false

        var radius = r.vec.distance(c.vec)
        when (mode) {
            BezierTest.cut -> {
                t1.enabled = true
                t2.enabled = true
            }

            BezierTest.integral, BezierTest.axis_intersection, BezierTest.nearest_point -> {
                s.enabled = true
            }

            BezierTest.line_intersect -> {
                p.enabled = true
                q.enabled = true
            }

            BezierTest.intersect_quadratic -> {
                qs.enabled = true
                qa.enabled = true
                qe.enabled = true

                quad.set(
                    qs.vec.x, qs.vec.y,
                    qa.vec.x, qa.vec.y,
                    qe.vec.x, qe.vec.y
                )
                drawQuad(ctx, CYAN)
            }

            BezierTest.intersect_cubic -> {
                cs.enabled = true
                ca.enabled = true
                cb.enabled = true
                ce.enabled = true

                cub.set(
                    cs.vec.x, cs.vec.y,
                    ca.vec.x, ca.vec.y,
                    cb.vec.x, cb.vec.y,
                    ce.vec.x, ce.vec.y
                )
                drawCub(ctx, CYAN)
            }

            BezierTest.intersect_circle -> {
                c.enabled = true
                r.enabled = true

                ctx.begin()
                ctx.circle(c.xf(), c.yf(), radius.toFloat())
                ctx.stroke(2f, BLUE + SOLID)
            }

            BezierTest.sample_length -> {
                l.enabled = true
            }

            BezierTest.subtract_curves -> {
                cs.enabled = true
                ca.enabled = true
                cb.enabled = true
                ce.enabled = true

                cub.set(
                    cs.vec.x, cs.vec.y,
                    ca.vec.x, ca.vec.y,
                    cb.vec.x, cb.vec.y,
                    ce.vec.x, ce.vec.y
                )
                drawCub(ctx, CYAN)
            }

            else -> {
            }
        }

        var error: String? = null

        when (mode) {
            BezierTest.none -> {
            }
            BezierTest.evaluation -> {
                ctx.begin()
                for (i in 0..TestConfig.density) {
                    val t = i / TestConfig.density.toDouble()

                    val x = bezier.x(t).toFloat()
                    val y = bezier.y(t).toFloat()

                    if (i == 0)
                        ctx.moveTo(x, y)
                    else
                        ctx.lineTo(x, y)
                }
                ctx.stroke(2f, SOLID + MAGENTA)

                for (i in 1..<TestConfig.density) {
                    val t = i / TestConfig.density.toDouble()

                    val x = bezier.x(t).toFloat()
                    val y = bezier.y(t).toFloat()

                    ctx.drawPointCircle(x, y, SOLID + MAGENTA, 6f)
                }
            }

            BezierTest.direction -> {
                for (i in 0..TestConfig.density) {
                    val t = i / TestConfig.density.toDouble()

                    val x = bezier.x(t).toFloat()
                    val y = bezier.y(t).toFloat()

                    val dx = bezier.dx(t).toFloat() / TestConfig.density
                    val dy = bezier.dy(t).toFloat() / TestConfig.density

                    ctx.drawSegment(x, y, x + dx, y + dy, SOLID + WHITE, 2f)
                    ctx.drawArrowEnd(x + dx, y + dy, dx, dy, 8f, SOLID + WHITE, 2f)
                }
            }

            BezierTest.bbox -> {
                bezier.boundingBox(bb)

                ctx.begin()
                ctx.rect(
                    bb.minX.toFloat(),
                    bb.minY.toFloat(),
                    bb.maxX.toFloat() - bb.minX.toFloat(),
                    bb.maxY.toFloat() - bb.minY.toFloat()
                )
                ctx.stroke(2f, SOLID + CYAN)
            }

            BezierTest.cut -> {
                val t1 = t1.vec.x
                val t2 = t2.vec.x
                bezier.cutPart(t1, t2)


                ctx.begin()
                drawActualBezier(ctx)
                ctx.stroke(2f, SOLID + CYAN)


                ctx.begin()
                drawControlLines(ctx)
                ctx.stroke(2f, TRANSPARENT + CYAN)

                drawControlPoints(ctx) { px, py ->
                    ctx.drawPointCircle(px.toFloat(), py.toFloat(), SOLID + CYAN, 8f)
                }
            }

            BezierTest.derivative -> {
                when (val bez = bezier) {
                    is Linear -> error = "The derivative of a linear curve is not a curve"
                    is Quadratic -> {
                        bez.derive(lin)
                        drawLin(ctx, RED)
                    }

                    is Cubic -> {
                        bez.derive(quad)
                        drawQuad(ctx, RED)
                    }
                }
            }

            BezierTest.second_derivative -> {
                when (val bez = bezier) {
                    is Linear -> error = "The second derivative of a linear curve is not a curve"
                    is Quadratic -> error = "The second derivative of a quadratic curve is not a curve"
                    is Cubic -> {
                        bez.deriveTwice(lin)
                        drawLin(ctx, RED)
                    }
                }
            }

            BezierTest.integral -> {
                when (val bez = bezier) {
                    is Linear -> {
                        bez.integrate(s.vec.x, s.vec.y, quad)
                        drawQuad(ctx, RED)
                    }

                    is Quadratic -> {
                        bez.integrate(s.vec.x, s.vec.y, cub)
                        drawCub(ctx, RED)
                    }

                    is Cubic -> error = "The integral of a cubic curve is of unsupported degree"
                }
            }

            BezierTest.polynomial_parity -> {
                when (val bez = bezier) {
                    is Linear -> {
                        lin.setXPolynomial(bez.x1, bez.x0)
                        lin.setYPolynomial(bez.y1, bez.y0)
                        drawLin(ctx, RED)
                    }

                    is Quadratic -> {
                        quad.setXPolynomial(bez.x2, bez.x1, bez.x0)
                        quad.setYPolynomial(bez.y2, bez.y1, bez.y0)
                        drawQuad(ctx, RED)
                    }

                    is Cubic -> {
                        cub.setXPolynomial(bez.x3, bez.x2, bez.x1, bez.x0)
                        cub.setYPolynomial(bez.y3, bez.y2, bez.y1, bez.y0)
                        drawCub(ctx, RED)
                    }
                }
            }

            BezierTest.axis_intersection -> {
                ctx.drawLine(s.xf(), s.yf(), s.xf(), s.yf() + 1, GREEN + SOLID, 2f)
                ctx.drawLine(s.xf(), s.yf(), s.xf() + 1, s.yf(), RED + SOLID, 2f)

                bezier.intersectHoriz(s.vec.y) { t ->
                    ctx.drawPointCircle(bezier.x(t).toFloat(), s.yf(), SOLID + CYAN, 10f)
                }
                bezier.intersectVert(s.vec.x) { t ->
                    ctx.drawPointCircle(s.xf(), bezier.y(t).toFloat(), SOLID + MAGENTA, 10f)
                }
            }

            BezierTest.nonlinearity -> {
                val nonlinearity = bezier.nonlinearity()

                val e = Vector2d(bezier.ex, bezier.ey)
                val s = Vector2d(bezier.sx, bezier.sy)
                val d = Vector2d(e).sub(s)

                val n = Vector2d(d).normalize().perpendicular().mul(nonlinearity)

                val s1 = Vector2d(s).add(n)
                val s2 = Vector2d(s).sub(n)

                val e1 = Vector2d(e).add(n)
                val e2 = Vector2d(e).sub(n)

                ctx.drawLine(
                    s1.x.toFloat(), s1.y.toFloat(),
                    e1.x.toFloat(), e1.y.toFloat(),
                    CYAN + TRANSPARENT, 2f
                )

                ctx.drawLine(
                    s2.x.toFloat(), s2.y.toFloat(),
                    e2.x.toFloat(), e2.y.toFloat(),
                    CYAN + TRANSPARENT, 2f
                )

                when (val bez = bezier) {
                    is Linear -> {
                        // Nothing to visualise, the line is per definition linear
                    }

                    is Quadratic -> {
                        val pcx = bez.run { (ex - sx) * 0.5 + sx }
                        val pcy = bez.run { (ey - sy) * 0.5 + sy }
                        val cx = bez.run { -(pcx - ax) / 2 } + pcx
                        val cy = bez.run { -(pcy - ay) / 2 } + pcy

                        ctx.drawSegment(
                            bez.sx.toFloat(), bez.sy.toFloat(), bez.ex.toFloat(), bez.ey.toFloat(),
                            CYAN + TRANSPARENT, 2f
                        )
                        ctx.drawSegment(
                            pcx.toFloat(), pcy.toFloat(), cx.toFloat(), cy.toFloat(),
                            CYAN + TRANSPARENT, 2f
                        )
                    }

                    is Cubic -> {
                        val pcx = bez.run { (ex - sx) * 0.33333333333333333333333 + sx }
                        val pcy = bez.run { (ey - sy) * 0.33333333333333333333333 + sy }
                        val cx = bez.run { -(pcx - ax) / 3 } + pcx
                        val cy = bez.run { -(pcy - ay) / 3 } + pcy

                        val pdx = bez.run { (ex - sx) * 0.66666666666666666666666 + sx }
                        val pdy = bez.run { (ey - sy) * 0.66666666666666666666666 + sy }
                        val dx = bez.run { -(pdx - bx) / 3 } + pdx
                        val dy = bez.run { -(pdy - by) / 3 } + pdy

                        ctx.drawSegment(
                            bez.sx.toFloat(), bez.sy.toFloat(), bez.ex.toFloat(), bez.ey.toFloat(),
                            CYAN + TRANSPARENT, 2f
                        )

                        ctx.drawSegment(
                            pcx.toFloat(), pcy.toFloat(), cx.toFloat(), cy.toFloat(),
                            CYAN + TRANSPARENT, 2f
                        )

                        ctx.drawSegment(
                            pdx.toFloat(), pdy.toFloat(), dx.toFloat(), dy.toFloat(),
                            CYAN + TRANSPARENT, 2f
                        )
                    }
                }

                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                val t = "nonlinearity = %.4f".format(nonlinearity)
                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.length -> {
                val len = bezier.length()
                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                val t = "length = %.4f".format(len)
                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.line_intersect -> {
                val px = p.vec.x
                val py = p.vec.y
                val qx = q.vec.x
                val qy = q.vec.y
                val vertical = abs(qx - px) < abs(qy - py)

                val col = if (vertical) YELLOW else BLUE
                ctx.drawLine(p.xf(), p.yf(), q.xf(), q.yf(), col + SOLID, 2f)

                val iscs = Intersections()
                val n = bezier.intersectLine(p.vec.x, p.vec.y, q.vec.x, q.vec.y, iscs)

                var cps = 0
                var lps = 0
                for ((t, u, x, y) in iscs) {
                    val tcol =
                        if (t >= 0 && t < 1) GREEN
                        else RED

                    ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + tcol, 8f)

                    val ucol =
                        if (u >= 0 && u < 1) GREEN
                        else RED

                    // If the calculation is correct, we should see the points
                    // be the same
                    val lx = lerp(px, qx, u).toFloat()
                    val ly = lerp(py, qy, u).toFloat()
                    ctx.drawPointCircleOut(lx, ly, SOLID + ucol, 12f, 2f)

                    if (t >= 0 && t < 1)
                        cps++
                    if (u >= 0 && u < 1)
                        lps++
                }

                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                val t = "$n intersection points, $cps on curve, $lps on line"
                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.to_cubic -> {
                when (val bez = bezier) {
                    is Linear -> cub.setLinear(bez)
                    is Quadratic -> cub.setQuadratic(bez)
                    is Cubic -> cub.set(bez)
                }

                drawCub(ctx, CYAN)
            }

            BezierTest.to_quadratics -> {
                val list = mutableListOf<Quadratic>()
                bezier.toQuadraticSpline(list)

                var i = false
                for (q in list) {
                    quad.set(q)
                    drawQuad(ctx, if (i) CYAN else YELLOW)
                    i = !i
                }
            }

            BezierTest.intersect_quadratic -> {
                val iscs = Intersections()
                when (val bez = bezier) {
                    is Linear -> bez.intersectQuadratic(quad, iscs)
                    is Quadratic -> bez.intersectQuadratic(quad, iscs)
                    is Cubic -> bez.intersectQuadratic(quad, iscs)
                }
                val n = iscs.size

                for ((t, u, x, y) in iscs) {
                    ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                }

                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                val t = "$n intersection points"
                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.intersect_cubic -> {
                val iscs = Intersections()
                when (val bez = bezier) {
                    is Linear -> bez.intersectCubic(cub, iscs)
                    is Quadratic -> bez.intersectCubic(cub, iscs)
                    is Cubic -> bez.intersectCubic(cub, iscs)
                }
                val n = iscs.size

                for ((t, u, x, y) in iscs) {
                    ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                }

                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                val t = "$n intersection points"
                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.cut_at_extrema -> {
                val spline = when (val bez = bezier) {
                    is Linear -> bez.cutAtExtrema()
                    is Quadratic -> bez.cutAtExtrema()
                    is Cubic -> bez.cutAtExtrema()
                }

                val colours = intArrayOf(
                    RED,
                    GREEN,
                    BLUE,
                    MAGENTA,
                    CYAN
                )

                for ((i, c) in spline.withIndex()) {
                    when (c) {
                        is Linear -> {
                            lin.set(c)
                            drawLin(ctx, colours[i])
                        }
                        is Quadratic -> {
                            quad.set(c)
                            drawQuad(ctx, colours[i])
                        }
                        is Cubic -> {
                            cub.set(c)
                            drawCub(ctx, colours[i])
                        }
                    }
                }
            }

            BezierTest.nearest_point -> {
                val t = bezier.nearestPoint(s.vec.x, s.vec.y)
                val x = bezier.x(t).toFloat()
                val y = bezier.y(t).toFloat()

                ctx.drawSegment(x, y, s.xf(), s.yf(), GREEN + TRANSPARENT, 2f)
                ctx.drawPointCircle(x, y, RED + SOLID, 8f)
            }

            BezierTest.intersect_circle -> {
                val iscs = Intersections()
                bezier.intersectCircle(c.vec.x, c.vec.y, radius, iscs)

                for ((t, angle, x, y) in iscs) {
                    circIsc(ctx, t, x, y, angle, c.vec, radius)
                }

                val n = iscs.size
                val t = "$n intersection points"

                ctx.textAlign(AlignX.RIGHT, AlignY.TOP)

                ctx.drawHudTextBg(t, ctx.windowW() - 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
                ctx.drawHudText(t, ctx.windowW() - 20f, 20f, SOLID + WHITE, 15f)

                ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
            }

            BezierTest.sample_length -> {
                val t = bezier.sampleLength(l.vec.x)
                if (t >= 0) {
                    val x = bezier.x(t)
                    val y = bezier.y(t)

                    ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                }
            }

            BezierTest.inflection -> {
                when (val bez = bezier) {
                    is Linear -> Unit
                    is Quadratic -> Unit
                    is Cubic -> bez.inflections { t ->
                        val x = bezier.x(t)
                        val y = bezier.y(t)

                        ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                    }
                }
            }

            BezierTest.max_curvature -> {
                when (val bez = bezier) {
                    is Linear -> Unit
                    is Quadratic -> Unit
                    is Cubic -> bez.curvatureExtrema { t ->
                        val x = bezier.x(t)
                        val y = bezier.y(t)

                        ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                    }
                }
            }

            BezierTest.find_cusp -> {
                when (val bez = bezier) {
                    is Linear -> {}
                    is Quadratic -> {
                        bez.derive(lin)
                        drawLin(ctx, RED)
                    }

                    is Cubic -> {
                        bez.derive(quad)
                        drawQuad(ctx, RED)
                    }
                }

                bezier.findCusps { t ->
                    val x = bezier.x(t)
                    val y = bezier.y(t)

                    ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + WHITE, 10f)
                }
            }

            BezierTest.subtract_curves -> {
                when (val bez = bezier) {
                    is Linear -> Unit
                    is Quadratic -> Unit
                    is Cubic -> bez.curvatureExtrema { t ->
                        val c = Cubic(
                            bez.sx - cub.sx,
                            bez.sy - cub.sy,
                            bez.ax - cub.ax,
                            bez.ay - cub.ay,
                            bez.bx - cub.bx,
                            bez.by - cub.by,
                            bez.ex - cub.ex,
                            bez.ey - cub.ey
                        )

                        drawCub(ctx, SOLID + RED, c)
                    }
                }
            }

            BezierTest.offset_lut -> {
                for (i in 0..60) {
                    val t = i / 60.0

                    val dx = bezier.dx(t)
                    val dy = bezier.dy(t)
                    val dl = sqrt(dx*dx + dy*dy)

                    val x = bezier.x(t)
                    val y = bezier.y(t)

                    val nx = -dy / dl
                    val ny = dx / dl

                    val ox = x + nx
                    val oy = y + ny
                    val ox2 = x - nx
                    val oy2 = y - ny

                    ctx.drawPointCircle(ox.toFloat(), oy.toFloat(), SOLID + WHITE, 3f)
                    ctx.drawPointCircle(ox2.toFloat(), oy2.toFloat(), SOLID + WHITE, 3f)
                }
            }

            BezierTest.curvature -> {
                for (i in 0..60) {
                    val t = i / 60.0

                    val dx = bezier.dx(t)
                    val dy = bezier.dy(t)
                    val dl = sqrt(dx*dx + dy*dy)

                    val ddx = bezier.ddx(t)
                    val ddy = bezier.ddy(t)

                    val kappa = (dx * ddy - dy * ddx) / (dx * dx + dy * dy).pow(1.5)

                    val x = bezier.x(t)
                    val y = bezier.y(t)

                    val nx = -dy / dl
                    val ny = dx / dl

                    val cx = x + nx * kappa
                    val cy = y + ny * kappa

                    ctx.drawPointCircle(cx.toFloat(), cy.toFloat(), SOLID + WHITE, 3f)

                    val ox = x + nx
                    val oy = y + ny
                    val ox2 = x - nx
                    val oy2 = y - ny

                    ctx.drawPointCircle(ox.toFloat(), oy.toFloat(), SOLID + WHITE, 3f)
                    ctx.drawPointCircle(ox2.toFloat(), oy2.toFloat(), SOLID + WHITE, 3f)
                }
            }

            BezierTest.arcapprox -> {
                when (val b = bezier) {
                    is Linear -> cub.setLinear(b)
                    is Quadratic -> cub.setQuadratic(b)
                    is Cubic -> cub.set(b)
                }
                with(cub) {
                    val rx = ex - sx
                    val ry = ey - sy
                    val rl = sqrt(rx * rx + ry * ry)

                    val px = ax - sx
                    val py = ay - sy
                    val pl = sqrt(px * px + py * py)

                    val qx = bx - ex
                    val qy = by - ey
                    val ql = sqrt(qx * qx + qy * qy)

                    val rnx = rx / rl
                    val rny = ry / rl

                    val pnx = px / pl
                    val pny = py / pl

                    val qnx = qx / ql
                    val qny = qy / ql

                    val psx = lerp(rnx, pnx, 0.5)
                    val psy = lerp(rny, pny, 0.5)

                    val qsx = lerp(-rnx, qnx, 0.5)
                    val qsy = lerp(-rny, qny, 0.5)

                    val snx = -rny
                    val sny = rnx

                    intersectLines(sx, sy, sx + psx, sy + psy, ex, ey, ex + qsx, ey + qsy) { u, v ->
                        if (u < 0 || v < 0) {
                            // return false
                        } else {
                            val ix = lerp(sx, sx + psx, u)
                            val iy = lerp(sy, sy + psy, u)

                            val isx = ix - sx
                            val isy = iy - sy

                            val iex = ix - ex
                            val iey = iy - ey

                            ctx.drawPointCircle(ix.toFloat(), iy.toFloat(), SOLID + WHITE, 6f)
                            ctx.drawSegment(sx.toFloat(), sy.toFloat(), ix.toFloat(), iy.toFloat(), SOLID + WHITE, 2f)
                            ctx.drawSegment(ex.toFloat(), ey.toFloat(), ix.toFloat(), iy.toFloat(), SOLID + WHITE, 2f)
                        }
                    }
//
//                    intersectLines(sx, sy, sx + psx, sy + psy, ex, ey, bx, by) { u, v ->
//                        if (u < 0 || v < 0) {
//                            // return false
//                        } else {
//                            val ix = lerp(ex, bx, v)
//                            val iy = lerp(ey, by, v)
//
//                            ctx.drawPointCircle(ix.toFloat(), iy.toFloat(), SOLID + WHITE, 6f)
//                            ctx.drawSegment(sx.toFloat(), sy.toFloat(), ix.toFloat(), iy.toFloat(), SOLID + WHITE, 2f)
//                        }
//                    }
                }
            }

            BezierTest.offset -> {
                val off = bezier.offset(1.0)
                drawSpline(ctx, SOLID + WHITE, off)

                for (curve in off) {
                    val c1 = when (val bez = bezier) {
                        is Linear -> Cubic().apply{ setLinear(bez) }
                        is Quadratic -> Cubic().apply{ setQuadratic(bez) }
                        is Cubic -> bez
                    }
                    val c2 = when (val bez = curve) {
                        is Linear -> Cubic().apply{ setLinear(bez) }
                        is Quadratic -> Cubic().apply{ setQuadratic(bez) }
                        is Cubic -> bez
                    }

                    val diff = Cubic(
                        c1.sx - c2.sx,
                        c1.sy - c2.sy,
                        c1.ax - c2.ax,
                        c1.ay - c2.ay,
                        c1.bx - c2.bx,
                        c1.by - c2.by,
                        c1.ex - c2.ex,
                        c1.ey - c2.ey
                    )

                    drawCub(ctx, SOLID+RED, diff)
                }
            }
        }

        updateBezier(ctx)
        ctx.begin()
        drawControlLines(ctx)
        ctx.stroke(2f, TRANSPARENT + YELLOW)

        if (error != null) {
            displayError(ctx, error)
        }
    }

    override fun drawHud(ctx: GeometryContext) {
        if (mode == BezierTest.none) {
            val t = "Drag points around. Hold CTRL to snap to integer coordinates.\n" +
                "To zoom, scroll or drag with ALT. Alternatively, use CTRL = or CTRL -.\n" +
                "To pan, drag using the middle mouse button or while holding SHIFT.\n" +
                "Press T to open the command line to change test or curve type.\n" +
                "Press G to show and hide the grid."

            ctx.textAlign(AlignX.CENTER, AlignY.BOTTOM)

            ctx.drawHudTextBoxBg(t, ctx.windowW() / 2f, ctx.windowH() - 20f, SOLID, ctx.windowW() - 20, 15f, 10f, 5f)
            ctx.drawHudTextBox(t, ctx.windowW() / 2f, ctx.windowH() - 20f, SOLID + CYAN, ctx.windowW() - 20, 15f)

            ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
        }
        if (mode == BezierTest.polynomial_parity) {
            val x = "x = %.2fx^3 + %.2fx^2 + %.2fx + %.2f".format(bezier.x3, bezier.x2, bezier.x1, bezier.x0)
            val y = "y = %.2fx^3 + %.2fx^2 + %.2fx + %.2f".format(bezier.y3, bezier.y2, bezier.y1, bezier.y0)

            ctx.textAlign(AlignX.LEFT, AlignY.TOP)

            ctx.drawHudTextBg(x, 20f, 20f, TRANSPARENT, 15f, 5f, 3f)
            ctx.drawHudText(x, 20f, 20f, SOLID + RED, 15f)

            ctx.drawHudTextBg(y, 20f, 45f, TRANSPARENT, 15f, 5f, 3f)
            ctx.drawHudText(y, 20f, 45f, SOLID + GREEN, 15f)

            ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
        }
    }
}
