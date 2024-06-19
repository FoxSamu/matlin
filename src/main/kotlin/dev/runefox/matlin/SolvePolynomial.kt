package dev.runefox.matlin

import kotlin.math.*

/**
 * Solves the equation `ax + b = 0` for `x` (in Kotlin syntax: `a*x + b == 0`). The result closure is
 * called for each solution giving `x` as a parameter. The returned value is the amount of solutions. The logic is as
 * follows:
 *
 * - If `a != 0`, then there is exactly 1 real solution and this is then returned.
 * - If `a == 0`, `b != 0`, then there are no solutions.
 * - Ohterwise, there technically are infinintely many solutions. In this case it will return the value `-1`
 */
inline fun solveLinear(a: Double, b: Double, result: (Double) -> Unit): Int {
    when {
        // Linear polynomial
        a != 0.0 -> {
            // bx + c = 0
            // bx = -c
            // x = -c/b
            result(-b / a)
            return 1
        }

        // Secretly a nonzero constant polynomial
        b != 0.0 -> {
            // c != 0, so for no x is there a value where c = 0
            return 0
        }

        // Very secretly a zero constant polynomial
        else -> {
            // Infinitely many values, special return code to indicate that
            return -1
        }
    }
}

/**
 * Solves the equation `ax^2 + bx + c = 0` for `x` (in Kotlin syntax: `a*x*x + b*x + c == 0`). The result closure is
 * called for each real solution. The returned value is the amount of solutions. The logic is as follows:
 *
 * - If `a != 0`, there are generally 2 solutions, but if these are complex it will not return any.
 * - Otherwise, it is the linear polynomial `bx + c = 0`, which it will solve as defined by [solveLinear].
 */
inline fun solveQuadratic(a: Double, b: Double, c: Double, result: (Double) -> Unit): Int {
    when {
        // True quadratic polynomial
        a != 0.0 -> {
            // Use quadratic formula
            val d = b * b - 4 * a * c
            return when {
                // Complex solutions
                d < 0 -> 0

                // Distinct real solutions
                d > 0 -> {
                    val sqrtd = sqrt(d)
                    result(-(b - sqrtd) / (2 * a))
                    result(-(b + sqrtd) / (2 * a))
                    2
                }

                // Equal solutions
                else -> {
                    result(-b / (2 * a))
                    1
                }
            }
        }

        // Secretly a linear polynomial
        else -> {
            return solveLinear(b, c, result)
        }
    }
}

/**
 * Solves the equation `ax^3 + bx^2 + cx + d = 0` for `x` (in Kotlin syntax: `a*x*x*x + b*x*x + c*x + d == 0`). The
 * result closure is called for each real solution. The returned value is the amount of solutions. The logic is as
 * follows:
 *
 * - If `a != 0`, there are generally 3 solutions, but two of these may be complex, in which case it only returns 1
 *   solution. If all solutions are the same it may return either 1 or 3 equal solutions.
 * - Otherwise, it is the quadratic polynomial `bx^2 + cx + d = 0`, which it will solve as defined by [solveQuadratic].
 */
inline fun solveCubic(a: Double, b: Double, c: Double, d: Double, result: (Double) -> Unit): Int {
    if (a == 0.0) {
        // Not a cubic equation!
        return solveQuadratic(b, c, d, result)
    }

    if (a != 0.0 && d == 0.0) {
        // Special case, as d = 0 we have:
        // ax^3 + bx^2 + cx = 0
        // Take x out of parentheses
        // (ax^2 = bx + c)x = 0

        // So x = 0 is a solution, and the solutions of (ax^2 = bx + c) = 0 are solutions
        result(0.0)
        return 1 + solveQuadratic(a, b, c, result)
    }

    if (b == 0.0 && c == 0.0) {
        // Special case, as b = c = 0 we have:
        // ax^3 + d = 0
        // Rewrite:
        // ax^3 = -d
        // x^3 = -d/a
        // x = cbrt(-d/a)
        // The other two roots must be complex, we don't need them
        result(cbrt(-d / a))
        return 1
    }



    // This is where it gets really dark

    // We need to compute a depressed cubic, i.e. one without a quadratic term.
    // We do this by a change of variable: t = x + b/3a.
    //
    // We get the following polynomial:
    // t^3 + pt + q
    val p: Double
    val q: Double

    // We'll find roots as values of t now. We have t = x-o, where o stands for
    // offset: o = b/3a.
    val o: Double

    if (b == 0.0) {
        // It is already depressed, skip a lot of unnecessary computation

        // t = x
        p = c / a
        q = d / a

        o = 0.0 // b = 0 therefore b/3a = 0
    } else {
        // Bully the cubic until it's depressed

        // t = x + b/3a
        val ac = a * c
        val abc = ac * b

        val a2 = a * a
        val a3 = a2 * a
        val b2 = b * b
        val b3 = b2 * b

        p = (3 * ac - b2) / (3 * a2)
        q = (2 * b3 - 9 * abc + 27 * a2 * d) / (27 * a3)

        o = b / (3 * a)
    }


    // Now we can use Cardan's method to solve the depressed cubic.

    val p3 = p * p * p
    val q2 = q * q
    val qh = q / 2
    val pt = p / 3

    val e = (q2 / 4) + (p3 / 27)

    // Calculate s = sqrt(e)
    if (abs(e) < 1e-20) {
        // s = 0 (somewhat): we have 1 root

        val v = cbrt(-qh)
        val t =
            if (abs(v) < 1e-20) 0.0 // We have 0/0, but the limit here goes to 0
            else v - pt * (1 / v)
        result(t - o)
        return 1
    }

    if (e >= 0) {
        // s is real: we have 1 root

        // What we'll get is that |sqrt(e)| = qh so v will become 0 while it shouldn't when qh > 0.
        // We can simply flip the sign of the square root to fix this.
        val sqrte =
            if (qh > 0) -sqrt(e)
            else sqrt(e)

        val v = cbrt(-qh + sqrte)
        val t =
            if (abs(v) < 1e-20) 0.0 // We have 0/0, but the limit here goes to 0
            else v - pt * (1 / v)
        result(t - o)
        return 1
    }

    // s is imaginary: we have 3 roots

    // Prelude:
    // hsqrt3 = sqrt(3) / 2
    val hsqrt3 = 1.7320508075688772935 / 2

    // f is the complex number -(q/2) + sqrt(e)
    val f = -qh
    val fi = -sqrt(-e)

    // Convert to polar coordinates
    val fMag = sqrt(f * f + fi * fi)
    val fArg = acos(f / fMag)

    // Compute cube root in polar (simple)
    val acMag = cbrt(fMag)
    val acArg = fArg / 3

    // Convert back to cartesian
    val ac = acMag * cos(acArg)
    val aci = acMag * sin(acArg)

    // Other cube roots can be computed by cartesian multiplication with the
    // 2 other 3rd roots of unity. Thankfully we have algebraic expressions for
    // these: -1/2 ± i sqrt(3) / 2.

    // All roots are real, so at the end of our calculation we will not have
    // nonzero imaginary values. We don't need to compute that so we can omit
    // the imaginary part of the calculation from here on.
    val bc = +aci * hsqrt3 - ac / 2
    val cc = -aci * hsqrt3 - ac / 2

    // We need to compute, for each root, c - (p/3)(1/c). Thus, we need the
    // complex multiplicative inverse of c. This is the conjugate divided by the
    // length squared.
    // We computed c from polar coordinates so we still have the length at
    // our disposal.
    val cr2 = acMag * acMag

    // The roots of the cubic are now all of the form c - (p/3)(c/cr2),
    // where c is either ac, bc or cc.
    result(ac - pt * (ac / cr2) - o)
    result(bc - pt * (bc / cr2) - o)
    result(cc - pt * (cc / cr2) - o)
    return 3
}
