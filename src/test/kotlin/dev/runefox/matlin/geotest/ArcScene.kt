package dev.runefox.matlin.geotest

import dev.runefox.matlin.Cubic
import dev.runefox.matlin.approximateArc
import net.shadew.geotest.GeometryContext
import net.shadew.geotest.Grid
import org.joml.Vector2d
import kotlin.math.atan2

class ArcScene : DraggablePointsScene() {
    val a = add(Point(null, GREEN + SOLID, Vector2d(1.0, 1.0)))
    val b = add(Point(null, GREEN + SOLID, Vector2d(-1.0, 1.0)))
    val c = add(Point(null, CYAN + SOLID, Vector2d(0.0, 0.0)))
    val r = add(Point(null, CYAN + SOLID, Vector2d(2.5, 0.0)))

    val grid = Grid()


    private fun drawCub(ctx: GeometryContext, col: Int, cub: Cubic) {
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

    override fun drawScene(ctx: GeometryContext) {
        grid.draw(ctx)

        val spline = approximateArc(
            c.vec.x,
            c.vec.y,
            r.vec.distance(c.vec),
            atan2(a.vec.y - c.vec.y, a.vec.x - c.vec.x),
            atan2(b.vec.y - c.vec.y, b.vec.x - c.vec.x),
            accuracy = 4
        )

        ctx.drawRay(c.xf(), c.yf(), a.xf(), a.yf(), GREEN + TRANSPARENT, 2f)
        ctx.drawRay(c.xf(), c.yf(), b.xf(), b.yf(), GREEN + TRANSPARENT, 2f)
        ctx.begin()
        ctx.circle(c.xf(),c.yf(),r.vec.distance(c.vec).toFloat())
        ctx.stroke(1f, CYAN + TRANSPARENT)

        for (curve in spline) {
            drawCub(ctx, WHITE, curve)
        }
    }

    override fun drawHud(ctx: GeometryContext) {
    }
}
