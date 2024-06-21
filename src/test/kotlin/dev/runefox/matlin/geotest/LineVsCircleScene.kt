package dev.runefox.matlin.geotest

import dev.runefox.matlin.intersectLineCircle
import dev.runefox.matlin.lerp
import net.shadew.geotest.GeometryContext
import net.shadew.geotest.Grid
import org.joml.Vector2d
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class LineVsCircleScene : DraggablePointsScene() {
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

    override fun drawScene(ctx: GeometryContext) {
        grid.draw(ctx)

        ctx.drawLine(a.xf(), a.yf(), b.xf(), b.yf(), GREEN + SOLID, 2f)

        val r = r.vec.distance(c.vec)
        ctx.begin()
        ctx.circle(c.xf(), c.yf(), r.toFloat())
        ctx.stroke(2f, BLUE + SOLID)

        intersectLineCircle(
            a.vec.x, a.vec.y,
            b.vec.x, b.vec.y,
            c.vec.x, c.vec.y,
            r
        ) { t1, t2, a1, a2 ->
            circIsc(ctx, t1, a1, a.vec, b.vec, c.vec, r)
            circIsc(ctx, t2, a2, a.vec, b.vec, c.vec, r)
        }
    }

    override fun drawHud(ctx: GeometryContext) {
    }
}
