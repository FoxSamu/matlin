package dev.runefox.matlin.geotest

import dev.runefox.matlin.Linear
import net.shadew.geotest.GeometryContext
import org.joml.Vector2d

class LinBezierScene : BezierScene() {
    override val bezier = Linear(
        -2.0, -2.0,
        2.0, 2.0
    )

    val s = add(Point("S", SOLID + YELLOW, Vector2d(bezier.sx, bezier.sy)))
    val e = add(Point("E", SOLID + YELLOW, Vector2d(bezier.ex, bezier.ey)))

    override fun updateBezier(ctx: GeometryContext) {
        bezier.sx = s.vec.x
        bezier.sy = s.vec.y
        bezier.ex = e.vec.x
        bezier.ey = e.vec.y
    }

    override fun drawActualBezier(ctx: GeometryContext) {
        ctx.moveTo(bezier.sx.toFloat(), bezier.sy.toFloat())
        ctx.lineTo(bezier.ex.toFloat(), bezier.ey.toFloat())
    }

    override fun drawControlLines(ctx: GeometryContext) {
        // Don't draw control handles as it's the same as the bezier
    }

    override fun drawControlPoints(ctx: GeometryContext, out: (Double, Double) -> Unit) {
        out(bezier.sx, bezier.sy)
        out(bezier.ex, bezier.ey)
    }
}
