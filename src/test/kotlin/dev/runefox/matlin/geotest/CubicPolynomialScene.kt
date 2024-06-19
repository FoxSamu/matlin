package dev.runefox.matlin.geotest

import dev.runefox.matlin.solveCubic
import net.shadew.geotest.AlignX
import net.shadew.geotest.AlignY
import net.shadew.geotest.GeometryContext
import net.shadew.geotest.Grid
import org.joml.Vector2d

class CubicPolynomialScene : DraggablePointsScene() {
    val a = add(Point("a", SOLID + YELLOW, Vector2d(16.0, 0.0)))
    val b = add(Point("b", SOLID + YELLOW, Vector2d(-24.0, 1.0)))
    val c = add(Point("c", SOLID + YELLOW, Vector2d(12.0, 2.0)))
    val d = add(Point("d", SOLID + YELLOW, Vector2d(-2.0, 3.0)))

    val grid = Grid().apply {
        xAxisColor = SOLID + RED
        yAxisColor = SOLID + GREEN
    }

    fun drawPolynomial(a: Double, b: Double, c: Double, d: Double, ctx: GeometryContext) {
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
        ctx.stroke(2f, SOLID + MAGENTA)
    }

    override fun drawScene(ctx: GeometryContext) {
        grid.draw(ctx)

        a.vec.y = 1.0
        b.vec.y = 2.0
        c.vec.y = 3.0
        d.vec.y = 4.0

        a.label = "a = %.2f".format(a.vec.x)
        b.label = "b = %.2f".format(b.vec.x)
        c.label = "c = %.2f".format(c.vec.x)
        d.label = "d = %.2f".format(d.vec.x)

        val a = a.vec.x
        val b = b.vec.x
        val c = c.vec.x
        val d = d.vec.x

        drawPolynomial(a, b, c, d, ctx)

        val x = ctx.mouseX().toDouble()
        val y = ((((a * x + b) * x) + c) * x + d)

        ctx.drawSegment(x.toFloat(), 0f, x.toFloat(), y.toFloat(), SOLID + CYAN, 2f)
        ctx.drawPointCircle(x.toFloat(), y.toFloat(), SOLID + CYAN, 8f)

        solveCubic(a, b, c, d) { p ->
            ctx.drawPointCircle(p.toFloat(), 0f, SOLID + WHITE, 10f)
        }
    }

    override fun drawHud(ctx: GeometryContext) {
        val a = a.vec.x
        val b = b.vec.x
        val c = c.vec.x
        val d = d.vec.x

        ctx.textAlign(AlignX.LEFT, AlignY.TOP)
        ctx.drawHudTextBg("%.2fx^3 + %.2fx^2 + %.2fx + %.2f = 0".format(a, b, c, d), 20f, 20f, TRANSPARENT, 15f, 3f, 3f)
        ctx.drawHudText("%.2fx^3 + %.2fx^2 + %.2fx + %.2f = 0".format(a, b, c, d), 20f, 20f, SOLID + WHITE, 15f)
    }
}
