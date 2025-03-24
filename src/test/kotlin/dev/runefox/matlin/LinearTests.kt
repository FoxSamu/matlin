package dev.runefox.matlin

import kotlin.test.Test
import kotlin.test.assertEquals

class LinearTests {
    @Test
    fun test2x2() {
        solveLinearSystem(
            5.0, 4.0,
            2.0, 3.0,
            58.0,
            33.0,
        ) { rx, ry ->
            assertEquals(6.0, rx, 0.001)
            assertEquals(7.0, ry, 0.001)
        }
    }

    @Test
    fun test3x3() {
        solveLinearSystem(
            6.0, 2.0, 1.0,
            4.0, 8.0, 2.0,
            7.0, 7.0, 5.0,
            17.0,
            22.0,
            36.0
        ) { rx, ry, rz ->
            assertEquals(2.0, rx, 0.001)
            assertEquals(1.0, ry, 0.001)
            assertEquals(3.0, rz, 0.001)
        }
    }

    @Test
    fun det4x4() {
        val det = det(
            7.0, 2.0, 3.0, 6.0,
            3.0, 2.0, 1.0, 6.0,
            4.0, 8.0, 5.0, 5.0,
            9.0, 4.0, 0.0, 1.0,
        )
        assertEquals(-972.0, det, 0.001)
    }

    @Test
    fun test4x4() {
        solveLinearSystem(
            7.0, 2.0, 3.0, 6.0,
            3.0, 2.0, 1.0, 6.0,
            4.0, 8.0, 5.0, 5.0,
            9.0, 4.0, 0.0, 1.0,
            77.0,
            53.0,
            79.0,
            62.0
        ) { rx, ry, rz, rw ->
            assertEquals(5.0, rx, 0.001)
            assertEquals(3.0, ry, 0.001)
            assertEquals(2.0, rz, 0.001)
            assertEquals(5.0, rw, 0.001)
        }
    }
}
