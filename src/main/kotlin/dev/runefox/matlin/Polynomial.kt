package dev.runefox.matlin

sealed interface Polynomial {
    val a0: Double
    val a1: Double
    val a2: Double
    val a3: Double

    fun f(x: Double): Double
    fun df(x: Double): Double
    fun ddf(x: Double): Double
    fun dddf(x: Double): Double

    fun d(): Polynomial
    fun dd(): Polynomial
    fun ddd(): Polynomial

    val degree: Int
}

object ZeroPolynomial : Polynomial {
    override val a0 get() = 0.0
    override val a1 get() = 0.0
    override val a2 get() = 0.0
    override val a3 get() = 0.0

    override fun f(x: Double) = 0.0
    override fun df(x: Double) = 0.0
    override fun ddf(x: Double) = 0.0
    override fun dddf(x: Double) = 0.0

    override fun d() = ZeroPolynomial
    override fun dd() = ZeroPolynomial
    override fun ddd() = ZeroPolynomial

    override val degree get() = 0

    fun i(c: Double = 0.0) = ConstantPolynomial(c)
}

class ConstantPolynomial(
    override var a0: Double = 0.0
) : Polynomial {
    override val a1 get() = 0.0
    override val a2 get() = 0.0
    override val a3 get() = 0.0

    override fun f(x: Double) = a1
    override fun df(x: Double) = 0.0
    override fun ddf(x: Double) = 0.0
    override fun dddf(x: Double) = 0.0

    override fun d() = ZeroPolynomial
    override fun dd() = ZeroPolynomial
    override fun ddd() = ZeroPolynomial

    override val degree get() = 0

    fun i(c: Double = 0.0) = LinearPolynomial(a0, c)
}

class LinearPolynomial(
    override var a0: Double = 0.0,
    override var a1: Double = 0.0
) : Polynomial {
    override val a2 get() = 0.0
    override val a3 get() = 0.0

    override fun f(x: Double) = a1 * x + a0
    override fun df(x: Double) = a1
    override fun ddf(x: Double) = 0.0
    override fun dddf(x: Double) = 0.0

    override fun d() = ConstantPolynomial(a1)
    override fun dd() = ZeroPolynomial
    override fun ddd() = ZeroPolynomial

    override val degree get() = 1

    fun i(c: Double = 0.0) = QuadraticPolynomial(1.0/2.0 * a1, a0, c)
}

class QuadraticPolynomial(
    override var a0: Double = 0.0,
    override var a1: Double = 0.0,
    override var a2: Double = 0.0
) : Polynomial {
    override val a3 get() = 0.0

    override fun f(x: Double) = (a2 * x + a1) * x + a0
    override fun df(x: Double) = 2 * a2 * x + a1
    override fun ddf(x: Double) = 0.0
    override fun dddf(x: Double) = 0.0

    override fun d() = LinearPolynomial(2 * a2, a1)
    override fun dd() = ConstantPolynomial(2 * a2)
    override fun ddd() = ZeroPolynomial

    override val degree get() = 2

    fun i(c: Double = 0.0) = CubicPolynomial(1.0/3.0 * a2, 1.0/2.0 * a1, a0, c)
}

class CubicPolynomial(
    override var a0: Double = 0.0,
    override var a1: Double = 0.0,
    override var a2: Double = 0.0,
    override var a3: Double = 0.0
) : Polynomial {

    override fun f(x: Double) = ((a3 * x + a2) * x + a1) * x + a0
    override fun df(x: Double) = (3 * a3 * x + 2 * a2) * x + a1
    override fun ddf(x: Double) = 6 * a3 * x + 2 * a2
    override fun dddf(x: Double) = 6 * a3

    override fun d() = QuadraticPolynomial(3 * a3, 2 * a2, a1)
    override fun dd() = LinearPolynomial(6 * a3, 2 * a2)
    override fun ddd() = ConstantPolynomial(6 * a3)

    override val degree get() = 3
}

fun ConstantPolynomial.solve(): Int {
    return if (a0 == 0.0) -1 else 0
}

inline fun LinearPolynomial.solve(result: (Double) -> Unit): Int {
    return solveLinear(a1, a0, result)
}

inline fun QuadraticPolynomial.solve(result: (Double) -> Unit): Int {
    return solveQuadratic(a2, a1, a0, result)
}

inline fun CubicPolynomial.solve(result: (Double) -> Unit): Int {
    return solveCubic(a3, a2, a1, a0, result)
}

inline fun Polynomial.solve(result: (Double) -> Unit): Int {
    return when (this) {
        is ZeroPolynomial -> -1
        is ConstantPolynomial -> solve()
        is LinearPolynomial -> solve(result)
        is QuadraticPolynomial -> solve(result)
        is CubicPolynomial -> solve(result)
    }
}
