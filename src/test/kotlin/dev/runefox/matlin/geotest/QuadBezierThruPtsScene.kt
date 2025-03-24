package dev.runefox.matlin.geotest

import dev.runefox.matlin.Linear
import dev.runefox.matlin.Quadratic
import dev.runefox.matlin.nearestPoint
import net.shadew.geotest.GeometryContext
import org.joml.Vector2d
import kotlin.math.sqrt

class QuadBezierThruPtsScene : BezierScene() {
    override val bezier = Quadratic(
        -2.0, -2.0,
        0.0, 2.0,
        2.0, -2.0
    )

    var dragT = 0.0

    val s = add(Point("S", SOLID + YELLOW, Vector2d(bezier.sx, bezier.sy)))
    val a = add(Point("A", SOLID + YELLOW, Vector2d(bezier.ax, bezier.ay)))
    val e = add(Point("E", SOLID + YELLOW, Vector2d(bezier.ex, bezier.ey)))


    override fun updateBezier(ctx: GeometryContext) {
        bezier.sx = s.vec.x
        bezier.sy = s.vec.y
        bezier.ax = a.vec.x
        bezier.ay = a.vec.y
        bezier.ex = e.vec.x
        bezier.ey = e.vec.y

        bezier.through3Points(
            s.vec.x, s.vec.y, 0.0,
            a.vec.x, a.vec.y, 0.5,
            e.vec.x, e.vec.y, 1.0
        )
    }

    override fun drawActualBezier(ctx: GeometryContext) {
        ctx.moveTo(bezier.sx.toFloat(), bezier.sy.toFloat())
        ctx.quadTo(bezier.ax.toFloat(), bezier.ay.toFloat(), bezier.ex.toFloat(), bezier.ey.toFloat())
    }

    override fun drawControlLines(ctx: GeometryContext) {
        ctx.moveTo(bezier.sx.toFloat(), bezier.sy.toFloat())
        ctx.lineTo(bezier.ax.toFloat(), bezier.ay.toFloat())
        ctx.lineTo(bezier.ex.toFloat(), bezier.ey.toFloat())
    }

    override fun drawControlPoints(ctx: GeometryContext, out: (Double, Double) -> Unit) {
        out(bezier.sx, bezier.sy)
        out(bezier.ax, bezier.ay)
        out(bezier.ex, bezier.ey)
    }
}
