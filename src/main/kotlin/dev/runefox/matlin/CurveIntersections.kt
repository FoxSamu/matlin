package dev.runefox.matlin

fun Bezier.intersectLinear(other: Linear, iscs: Intersections) {
    intersectLine(other.sx, other.sy, other.ex, other.ey, iscs)
    iscs.removeIf { t, u, _, _ -> t < 0 || t > 1 || u < 0 || u > 1 }
}

fun Linear.intersectQuadratic(other: Quadratic, iscs: Intersections) {
    other.intersectLinear(this, iscs)
    iscs.swapTU()
}

fun Quadratic.intersectQuadratic(
    other: Quadratic,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
) {
    intersectQuadQuad(this, other, BoundingBox(), BoundingBox(), iscs, nonlinearityThreshold)
}

fun Cubic.intersectQuadratic(
    other: Quadratic,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
) {
    intersectQuadCub(other, this, BoundingBox(), BoundingBox(), iscs, nonlinearityThreshold)
    iscs.swapTU()
}

fun Linear.intersectCubic(other: Cubic, iscs: Intersections) {
    other.intersectLinear(this, iscs)
    iscs.swapTU()
}

fun Quadratic.intersectCubic(
    other: Cubic,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
) {
    intersectQuadCub(this, other, BoundingBox(), BoundingBox(), iscs, nonlinearityThreshold)
}

fun Cubic.intersectCubic(
    other: Cubic,
    iscs: Intersections,
    nonlinearityThreshold: Double = defThreshold
) {
    intersectCubCub(this, other, BoundingBox(), BoundingBox(), iscs, nonlinearityThreshold)
}

private fun intersectQuadQuad(
    a: Quadratic,
    b: Quadratic,
    abox: BoundingBox,
    bbox: BoundingBox,
    iscs: Intersections,
    nonlinearityThreshold: Double
): Int {
    a.boundingBox(abox)
    b.boundingBox(bbox)

    if (!(abox overlaps bbox)) {
        return 0
    }

    val nla = a.nonlinearity()
    val nlb = b.nonlinearity()

    if (nla < nonlinearityThreshold && nlb < nonlinearityThreshold) {
        val r = intersectLines(
            a.sx, a.sy,
            a.ex, a.ey,
            b.sx, b.sy,
            b.ex, b.ey
        ) { t, u ->
            if (t >= 0 && t < 1 && u >= 0 && u < 1) {
                iscs.add(t, u, a.x(t), a.y(t))
            }
        }
        return if (r) 1 else 0
    }
    val (asx, asy, aax, aay, aex, aey) = a
    val (bsx, bsy, bax, bay, bex, bey) = b

    if (nla < nonlinearityThreshold) {
        b.cutStart(0.5)
        val f = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bex, bey)

        b.cutEnd(0.5)
        val s = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bex, bey)
        return f + s
    }

    if (nlb < nonlinearityThreshold) {
        a.cutStart(0.5)
        val f = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, aex, aey)

        a.cutEnd(0.5)
        val s = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, aex, aey)
        return f + s
    }


    b.cutStart(0.5)
    a.cutStart(0.5)
    val ff = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)

    a.cutEnd(0.5)
    val fs = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)
    b.set(bsx, bsy, bax, bay, bex, bey)

    b.cutEnd(0.5)
    a.cutStart(0.5)
    val sf = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)

    a.cutEnd(0.5)
    val ss = intersectQuadQuad(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)
    b.set(bsx, bsy, bax, bay, bex, bey)

    return ff + fs + sf + ss
}


private fun intersectQuadCub(
    a: Quadratic,
    b: Cubic,
    abox: BoundingBox,
    bbox: BoundingBox,
    iscs: Intersections,
    nonlinearityThreshold: Double
): Int {
    a.boundingBox(abox)
    b.boundingBox(bbox)

    if (!(abox overlaps bbox)) {
        return 0
    }

    val nla = a.nonlinearity()
    val nlb = b.nonlinearity()

    if (nla < nonlinearityThreshold && nlb < nonlinearityThreshold) {
        val r = intersectLines(
            a.sx, a.sy,
            a.ex, a.ey,
            b.sx, b.sy,
            b.ex, b.ey
        ) { t, u ->
            if (t >= 0 && t < 1 && u >= 0 && u < 1) {
                iscs.add(t, u, a.x(t), a.y(t))
            }
        }
        return if (r) 1 else 0
    }
    val (asx, asy, aax, aay, aex, aey) = a
    val (bsx, bsy, bax, bay, bbx, bby, bex, bey) = b

    if (nla < nonlinearityThreshold) {
        b.cutStart(0.5)
        val f = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

        b.cutEnd(0.5)
        val s = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)
        return f + s
    }

    if (nlb < nonlinearityThreshold) {
        a.cutStart(0.5)
        val f = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, aex, aey)

        a.cutEnd(0.5)
        val s = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, aex, aey)
        return f + s
    }


    b.cutStart(0.5)
    a.cutStart(0.5)
    val ff = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)

    a.cutEnd(0.5)
    val fs = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)
    b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

    b.cutEnd(0.5)
    a.cutStart(0.5)
    val sf = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)

    a.cutEnd(0.5)
    val ss = intersectQuadCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, aex, aey)
    b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

    return ff + fs + sf + ss
}


private fun intersectCubCub(
    a: Cubic,
    b: Cubic,
    abox: BoundingBox,
    bbox: BoundingBox,
    iscs: Intersections,
    nonlinearityThreshold: Double
): Int {
    a.boundingBox(abox)
    b.boundingBox(bbox)

    if (!(abox overlaps bbox)) {
        return 0
    }

    val nla = a.nonlinearity()
    val nlb = b.nonlinearity()

    if (nla < nonlinearityThreshold && nlb < nonlinearityThreshold) {
        val r = intersectLines(
            a.sx, a.sy,
            a.ex, a.ey,
            b.sx, b.sy,
            b.ex, b.ey
        ) { t, u ->
            if (t >= 0 && t < 1 && u >= 0 && u < 1) {
                iscs.add(t, u, a.x(t), a.y(t))
            }
        }
        return if (r) 1 else 0
    }
    val (asx, asy, aax, aay, abx, aby, aex, aey) = a
    val (bsx, bsy, bax, bay, bbx, bby, bex, bey) = b

    if (nla < nonlinearityThreshold) {
        b.cutStart(0.5)
        val f = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

        b.cutEnd(0.5)
        val s = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)
        return f + s
    }

    if (nlb < nonlinearityThreshold) {
        a.cutStart(0.5)
        val f = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, abx, aby, aex, aey)

        a.cutEnd(0.5)
        val s = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
        a.set(asx, asy, aax, aay, abx, aby, aex, aey)
        return f + s
    }


    b.cutStart(0.5)
    a.cutStart(0.5)
    val ff = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, abx, aby, aex, aey)

    a.cutEnd(0.5)
    val fs = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, abx, aby, aex, aey)
    b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

    b.cutEnd(0.5)
    a.cutStart(0.5)
    val sf = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, abx, aby, aex, aey)

    a.cutEnd(0.5)
    val ss = intersectCubCub(a, b, abox, bbox, iscs, nonlinearityThreshold)
    a.set(asx, asy, aax, aay, abx, aby, aex, aey)
    b.set(bsx, bsy, bax, bay, bbx, bby, bex, bey)

    return ff + fs + sf + ss
}
