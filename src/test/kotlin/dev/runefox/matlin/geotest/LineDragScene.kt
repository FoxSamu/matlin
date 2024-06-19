package dev.runefox.matlin.geotest

import dev.runefox.matlin.clamp01
import dev.runefox.matlin.lerp
import dev.runefox.matlin.projectOnLine
import net.shadew.geotest.GeometryContext
import net.shadew.geotest.Grid
import org.joml.Vector2d

class LineDragScene : DraggablePointsScene() {
    val grid = Grid()

    val a = add(Point(null, YELLOW + SOLID, Vector2d(-1.0, -2.0)))
    val b = add(Point(null, YELLOW + SOLID, Vector2d(1.0, 2.0)))
    val t = add(Point("t", YELLOW + SOLID, Vector2d(-1.0 / 2, -2.0 / 2)))
    val u = add(Point("u", YELLOW + SOLID, Vector2d(0.0, 0.0)))
    val p = add(Point(null, YELLOW + SOLID, Vector2d(-1.0, 1.0)))

    val ma = Vector2d()
    val mb = Vector2d()

    override fun drawScene(ctx: GeometryContext) {
        grid.draw(ctx)

        val u = clamp01(projectOnLine(a.vec.x, a.vec.y, b.vec.x, b.vec.y, u.vec.x, u.vec.y))
        a.vec.lerp(b.vec, u, this.u.vec)

        val t = clamp01(projectOnLine(a.vec.x, a.vec.y, b.vec.x, b.vec.y, this.t.vec.x, this.t.vec.y))
        a.vec.lerp(b.vec, t, this.t.vec)

        val ax = a.vec.x
        val ay = a.vec.y

        val bx = b.vec.x
        val by = b.vec.y

        val px = p.vec.x
        val py = p.vec.y


//        a.vec.lerp(p.vec, 1-u, ma)
//        b.vec.lerp(p.vec, u, mb)

        // P - (Bt + A(1-t))

        // ===============================
        // U = 0
        // A = A + P - (Bt + A(1-t))
        // B = B + P - (Bt + A(1-t))

        // A = P - Bt - A(2-t)
        // B = P - B(t+1) - A(1-t)

//        ma.x = px - bx * t - ax * (2 - t)
//        ma.y = py - by * t - ay * (2 - t)
//
//        mb.x = px - bx * (t + 1) - ax * (1 - t)
//        mb.y = py - by * (t + 1) - ay * (1 - t)


        // ===============================
        // U = -1
        // A = (P - Bt)/(1-t)
        // B = B

        // ===============================
        // U = 1
        // A = A
        // B = (P - A(1-t))/t

        val tx = (bx - ax) * t + ax
        val ty = (by - ay) * t + ay
        val dx = px - tx
        val dy = py - ty

        val an = Vector2d(px - ax, py - ay).normalize()
        val bn = Vector2d(px - bx, py - by).normalize()

        ctx.drawLine(a.xf(), a.yf(), a.xf() + dx.toFloat(), a.yf() + dy.toFloat(), WHITE + TRANSPARENT, 2f)
        ctx.drawLine(b.xf(), b.yf(), b.xf() + dx.toFloat(), b.yf() + dy.toFloat(), WHITE + TRANSPARENT, 2f)

        ctx.drawNormal(a.xf(), a.yf(), an.x.toFloat(), an.y.toFloat(), 20f, WHITE + SOLID, 2f)
        ctx.drawNormal(b.xf(), b.yf(), bn.x.toFloat(), bn.y.toFloat(), 20f, WHITE + SOLID, 2f)

        val ru = 2 * u - 1
        val fu = if (ru < 0) {
            lerp(t, 0.0, -ru)
        } else {
            lerp(t, 1.0, ru)
        }

        ma.x = ((px - bx * t) / (1 - t)) * (1 - fu) + ax * fu
        ma.y = ((py - by * t) / (1 - t)) * (1 - fu) + ay * fu

        mb.x = ((px - ax * (1 - t)) / t) * fu + bx * (1 - fu)
        mb.y = ((py - ay * (1 - t)) / t) * fu + by * (1 - fu)


        ctx.drawSegment(
            a.xf(), a.yf(),
            b.xf(), b.yf(),
            YELLOW + SOLID,
            2f
        )


        ctx.drawSegment(
            ma.x.toFloat(), ma.y.toFloat(),
            mb.x.toFloat(), mb.y.toFloat(),
            CYAN + SOLID,
            2f
        )
    }

    override fun drawHud(ctx: GeometryContext) {
    }
}
