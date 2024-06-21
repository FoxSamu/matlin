package dev.runefox.matlin.geotest

import net.shadew.geotest.*
import org.joml.Vector2d

abstract class DraggablePointsScene : GeomScene(), MouseDown, MouseUp {
    private val points: MutableList<Point> = ArrayList()

    private var hoverPoint = -1
    private var dragPoint = -1

    protected fun isDragging(point: Point): Boolean {
        val i = points.indexOf(point)
        return i >= 0 && dragPoint == i
    }

    protected fun isHovering(point: Point): Boolean {
        val i = points.indexOf(point)
        return i >= 0 && hoverPoint == i
    }

    protected fun add(point: Point): Point {
        points.add(point)
        return point
    }

    protected fun moveToBack(point: Point) {
        if (points.remove(point))
            points.add(0, point)
    }

    protected fun moveToFront(point: Point) {
        if (points.remove(point))
            points.add(point)
    }

    override fun init(ctx: GeometryContext) {
        ctx.onMouseDown(this)
        ctx.onMouseUp(this)
    }

    protected abstract fun drawScene(ctx: GeometryContext)
    protected abstract fun drawHud(ctx: GeometryContext)

    protected fun onGrab(ctx: GeometryContext, pt: Point) {

    }

    protected fun onDrop(ctx: GeometryContext, pt: Point) {

    }

    override fun draw(ctx: GeometryContext) {
        ctx.font("JetBrainsMono-Bold")

        if (dragPoint >= 0) {
            var pt = points.get(dragPoint)

            var x = ctx.mouseX()
            var y = ctx.mouseY()
            if (ctx.keyDown(Input.KEY_LEFT_CONTROL) || ctx.keyDown(Input.KEY_RIGHT_CONTROL)) {
                x = Math.round(x).toFloat()
                y = Math.round(y).toFloat()
            }
            pt.vec.set(x.toDouble(), y.toDouble())
        }

        drawScene(ctx)

        hoverPoint = -1
        var point = 0
        for (pt in points) {
            if (!pt.enabled) {
                point++
                continue
            }
            var dx = ctx.lenToScreen(pt.xf() - ctx.mouseX())
            var dy = ctx.lenToScreen(pt.yf() - ctx.mouseY())
            if (dx * dx + dy * dy < 12 * 12) {
                hoverPoint = point
            }
            point++
        }

        if (dragPoint <= 0 && hoverPoint >= 0) {
            var pt = points.get(hoverPoint)
            ctx.drawPointCircleOut(pt.xf(), pt.yf(), pt.colour, 12f, 2f)
        }

        if (dragPoint >= 0) {
            var pt = points.get(dragPoint)
            ctx.drawPointCircleOut(pt.xf(), pt.yf(), pt.colour, 12f, 2f)
        }


        for (pt in points) {
            if (!pt.enabled)
                continue
            ctx.drawPointCircle(pt.xf(), pt.yf(), pt.colour, 8f)
        }

        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
        for (pt in points) {
            if (!pt.enabled)
                continue
            if (pt.label != null) {
                ctx.drawTextBg(pt.label, pt.xf(), pt.yf(), 0f, -25f, -0x78000000, 16f, 8f, 3f)
            }
        }

        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE)
        for (pt in points) {
            if (!pt.enabled)
                continue
            if (pt.label != null) {
                ctx.drawText(pt.label, pt.xf(), pt.yf(), 0f, -25f, pt.colour, 16f)
            }
        }

        drawHud(ctx)
    }

    override fun mouseDown(ctx: GeometryContext, button: Int, mods: Int) {
        dragPoint = hoverPoint
        if (dragPoint >= 0)
            onGrab(ctx, points.get(dragPoint))
    }

    override fun mouseUp(ctx: GeometryContext, button: Int, mods: Int) {
        if (dragPoint >= 0)
            onDrop(ctx, points.get(dragPoint))
        dragPoint = -1
    }

    override fun stop(ctx: GeometryContext) {
        ctx.removeMouseDown(this)
        ctx.removeMouseUp(this)
    }

    data class Point(var label: String?, var colour: Int, val vec: Vector2d, var enabled: Boolean = true) {
        fun xf(): Float {
            return vec.x.toFloat()
        }

        fun yf(): Float {
            return vec.y.toFloat()
        }
    }
}
