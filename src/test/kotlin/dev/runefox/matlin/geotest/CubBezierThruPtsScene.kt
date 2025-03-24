package dev.runefox.matlin.geotest

import dev.runefox.matlin.Cubic
import dev.runefox.matlin.Linear
import dev.runefox.matlin.Quadratic
import dev.runefox.matlin.nearestPoint
import net.shadew.geotest.GeometryContext
import org.joml.Vector2d

class CubBezierThruPtsScene : BezierScene() {
    override val bezier = Cubic(
        -4.0, -2.0,
        -2.0, 2.0,
        2.0, -2.0,
        4.0, 2.0,
    )

    var dragT = 0.0

    val s = add(Point("S", SOLID + YELLOW, Vector2d(bezier.sx, bezier.sy)))
    val a = add(Point("A", SOLID + YELLOW, Vector2d(bezier.ax, bezier.ay)))
    val b = add(Point("B", SOLID + YELLOW, Vector2d(bezier.bx, bezier.by)))
    val e = add(Point("E", SOLID + YELLOW, Vector2d(bezier.ex, bezier.ey)))

    override fun updateBezier(ctx: GeometryContext) {
        bezier.through4Points(
            s.vec.x, s.vec.y, 0.0,
            a.vec.x, a.vec.y, 0.33333333333333333333333,
            b.vec.x, b.vec.y, 0.66666666666666666666667,
            e.vec.x, e.vec.y, 1.0,
        )
    }

    override fun drawActualBezier(ctx: GeometryContext) {
        ctx.moveTo(bezier.sx.toFloat(), bezier.sy.toFloat())
        ctx.cubicTo(
            bezier.ax.toFloat(),
            bezier.ay.toFloat(),
            bezier.bx.toFloat(),
            bezier.by.toFloat(),
            bezier.ex.toFloat(),
            bezier.ey.toFloat()
        )
    }

    override fun drawControlLines(ctx: GeometryContext) {
        ctx.moveTo(bezier.sx.toFloat(), bezier.sy.toFloat())
        ctx.lineTo(bezier.ax.toFloat(), bezier.ay.toFloat())
        ctx.lineTo(bezier.bx.toFloat(), bezier.by.toFloat())
        ctx.lineTo(bezier.ex.toFloat(), bezier.ey.toFloat())
    }

    override fun drawControlPoints(ctx: GeometryContext, out: (Double, Double) -> Unit) {
        out(bezier.sx, bezier.sy)
        out(bezier.ax, bezier.ay)
        out(bezier.bx, bezier.by)
        out(bezier.ex, bezier.ey)
    }
}
