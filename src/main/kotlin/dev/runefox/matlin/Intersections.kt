package dev.runefox.matlin

class Intersections(initCapacity: Int = 4) : Iterable<Intersection> {
    private var elements = 0
    private var ts = DoubleArray(initCapacity)
    private var us = DoubleArray(initCapacity)
    private var xs = DoubleArray(initCapacity)
    private var ys = DoubleArray(initCapacity)

    private val capacity get() = ts.size

    private fun doubleSize() {
        val nc = capacity * 2
        ts = ts.copyOf(nc)
        us = us.copyOf(nc)
        xs = xs.copyOf(nc)
        ys = ys.copyOf(nc)
    }

    private fun checkIndex(index: Int): Int {
        if (index < 0 || index >= elements) {
            throw IndexOutOfBoundsException("index: $index, size: $elements")
        }
        return index
    }

    private fun checkAddIndex(index: Int): Int {
        if (index < 0 || index > elements) {
            throw IndexOutOfBoundsException("index: $index, size: $elements")
        }
        return index
    }

    val size get() = elements
    val indices get() = 0..<elements

    fun t(index: Int) = ts[checkIndex(index)]
    fun u(index: Int) = us[checkIndex(index)]
    fun x(index: Int) = xs[checkIndex(index)]
    fun y(index: Int) = ys[checkIndex(index)]

    operator fun get(index: Int, out: Intersection = Intersection()): Intersection {
        checkIndex(index)
        out.t = ts[index]
        out.u = us[index]
        out.x = xs[index]
        out.y = ys[index]
        return out
    }

    fun clear() {
        elements = 0
    }

    fun add(index: Int, t: Double, u: Double, x: Double, y: Double) {
        checkAddIndex(index)
        if (elements == capacity)
            doubleSize()

        if (index < elements) {
            ts.copyInto(ts, index + 1, index, elements)
            us.copyInto(us, index + 1, index, elements)
            xs.copyInto(xs, index + 1, index, elements)
            ys.copyInto(ys, index + 1, index, elements)
        }

        ts[index] = t
        us[index] = u
        xs[index] = x
        ys[index] = y
        elements ++
    }

    fun add(index: Int, isc: Intersection) {
        add(index, isc.t, isc.u, isc.x, isc.y)
    }

    fun add(t: Double, u: Double, x: Double, y: Double) {
        add(size, t, u, x, y)
    }

    fun add(isc: Intersection) {
        add(isc.t, isc.u, isc.x, isc.y)
    }

    fun set(index: Int, t: Double, u: Double, x: Double, y: Double) {
        checkIndex(index)
        ts[index] = t
        us[index] = u
        xs[index] = x
        ys[index] = y
    }

    fun set(index: Int, isc: Intersection) {
        set(index, isc.t, isc.u, isc.x, isc.y)
    }

    fun remove(index: Int) {
        checkIndex(index)
        val ns = elements - 1

        if (index < ns) {
            ts.copyInto(ts, index, index + 1, elements)
            us.copyInto(us, index, index + 1, elements)
            xs.copyInto(xs, index, index + 1, elements)
            ys.copyInto(ys, index, index + 1, elements)
        }

        elements = ns
    }

    inline fun removeIf(pred: (Double, Double, Double, Double) -> Boolean) {
        for (i in indices.reversed()) {
            if (pred(t(i), u(i), x(i), y(i))) {
                remove(i)
            }
        }
    }

    fun swapTU() {
        for (i in indices) {
            set(i, u(i), t(i), x(i), y(i))
        }
    }

    override fun iterator(): Iterator<Intersection> {
        return Iter()
    }

    private inner class Iter : Iterator<Intersection> {
        private val isc = Intersection()
        private var idx = 0

        override fun hasNext(): Boolean {
            return idx < size
        }

        override fun next(): Intersection {
            get(idx, isc)
            idx++
            return isc
        }
    }
}
