package dev.runefox.matlin

interface SplineOutput {
    fun addLine(sx: Double, sy: Double, ex: Double, ey: Double)
    fun addQuadratic(sx: Double, sy: Double, ax: Double, ay: Double, ex: Double, ey: Double)
    fun addCubic(sx: Double, sy: Double, ax: Double, ay: Double, bx: Double, by: Double, ex: Double, ey: Double)
    fun addArc(cx: Double, cy: Double, rx: Double, startAngle: Double, endAngle: Double, clockwise: Boolean)
}
