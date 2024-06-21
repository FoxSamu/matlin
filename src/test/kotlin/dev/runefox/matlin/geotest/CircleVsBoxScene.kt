package dev.runefox.matlin.geotest

import dev.runefox.matlin.BoundingBox
import dev.runefox.matlin.lerp
import net.shadew.geotest.GeometryContext
import net.shadew.geotest.Grid
import org.joml.Vector2d
import kotlin.math.*

class CircleVsBoxScene : DraggablePointsScene() {
    val a = add(Point(null, GREEN + SOLID, Vector2d(-2.0, -1.0)))
    val b = add(Point(null, GREEN + SOLID, Vector2d(1.0, 2.0)))
    val c = add(Point(null, BLUE + SOLID, Vector2d(0.0, 0.0)))
    val r = add(Point(null, BLUE + SOLID, Vector2d(2.5, 0.0)))

    val grid = Grid()

    fun circIsc(ctx: GeometryContext, t: Double, angle: Double, a: Vector2d, b: Vector2d, c: Vector2d, r: Double) {
        ctx.drawPointCircle(
            lerp(a.x, b.x, t).toFloat(),
            lerp(a.y, b.y, t).toFloat(),
            CYAN + SOLID,
            10f
        )

        val sin = sin(angle) * r
        val cos = cos(angle) * r

        ctx.drawSegment(c.x.toFloat(), c.y.toFloat(), (c.x + cos).toFloat(), (c.y + sin).toFloat(), RED + TRANSPARENT, 2f)

        var deg = angle/PI*180
        if (deg > 359.951f)
            deg = 0.0

        val text = "%.1f°".format(deg)
        ctx.drawTextBg(text, (c.x + cos/2).toFloat(), (c.y + sin/2).toFloat(), 0f, 0f, TRANSPARENT, 12f, 4f, 2f)
        ctx.drawText(text, (c.x + cos/2).toFloat(), (c.y + sin/2).toFloat(), 0f, 0f, RED + SOLID, 12f)
    }

    val bbox = BoundingBox()

    override fun drawScene(ctx: GeometryContext) {
        grid.draw(ctx)

        bbox.minX = min(a.vec.x, b.vec.x)
        bbox.minY = min(a.vec.y, b.vec.y)
        bbox.maxX = max(a.vec.x, b.vec.x)
        bbox.maxY = max(a.vec.y, b.vec.y)

        val r = r.vec.distance(c.vec)
        val col = if (bbox.intersectsCircle(c.vec.x, c.vec.y, r))
            GREEN + SOLID
        else
            RED + SOLID

        ctx.begin()
        ctx.rect(a.xf(), a.yf(), b.xf() - a.xf(), b.yf() - a.yf())
        ctx.stroke(2f, col)

        ctx.begin()
        ctx.circle(c.xf(), c.yf(), r.toFloat())
        ctx.stroke(2f, BLUE + SOLID)
    }

    override fun drawHud(ctx: GeometryContext) {
    }
}
