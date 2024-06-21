package dev.runefox.matlin.geotest

import dev.runefox.matlin.Cubic
import dev.runefox.matlin.Linear
import dev.runefox.matlin.Quadratic
import dev.runefox.matlin.nearestPoint
import net.shadew.geotest.GeometryContext
import org.joml.Vector2d

class CubBezierScene : BezierScene() {
    override val bezier = Cubic(
        -4.0, -2.0,
        -2.0, 2.0,
        2.0, -2.0,
        4.0, 2.0,
    )

    var dragT = 0.0
    val m = add(Point(null, SOLID + MAGENTA, Vector2d(), false))
    init {
        moveToBack(m)
    }

    val s = add(Point("S", SOLID + YELLOW, Vector2d(bezier.sx, bezier.sy)))
    val a = add(Point("A", SOLID + YELLOW, Vector2d(bezier.ax, bezier.ay)))
    val b = add(Point("B", SOLID + YELLOW, Vector2d(bezier.bx, bezier.by)))
    val e = add(Point("E", SOLID + YELLOW, Vector2d(bezier.ex, bezier.ey)))

    override fun updateBezier(ctx: GeometryContext) {
        bezier.sx = s.vec.x
        bezier.sy = s.vec.y
        bezier.ax = a.vec.x
        bezier.ay = a.vec.y
        bezier.bx = b.vec.x
        bezier.by = b.vec.y
        bezier.ex = e.vec.x
        bezier.ey = e.vec.y

        if (isDragging(m)) {
            m.enabled = true
            bezier.drag(dragT, m.vec.x, m.vec.y)
            a.vec.x = bezier.ax
            a.vec.y = bezier.ay
            b.vec.x = bezier.bx
            b.vec.y = bezier.by
        } else {
            m.enabled = false
            dragT = bezier.nearestPoint(ctx.mouseX().toDouble(), ctx.mouseY().toDouble())
            val bx = bezier.x(dragT)
            val by = bezier.y(dragT)
            val dx = bx - ctx.mouseX()
            val dy = by - ctx.mouseY()
            val o = ctx.lenToWorld(5f)
            if (dx * dx + dy * dy < o * o) {
                m.enabled = true
                m.vec.x = bx
                m.vec.y = by
            }
        }
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
