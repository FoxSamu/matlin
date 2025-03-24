package dev.runefox.matlin.geotest

import net.shadew.geotest.CommandHandler
import net.shadew.geotest.GeometryTest

class GeoTest : GeometryTest(), CommandHandler {
    var scene: BezierScene = CubBezierScene()

    override fun init() {
        scene(scene)
    }

    private val suggestions = BezierTest.entries.map { it.name } + BezierType.entries.map { it.name }

    override fun process(input: String, cursor: Int, processor: CommandHandler.Processor) {
        for (sugg in suggestions) {
            if (sugg.startsWith(input.substring(0, cursor)))
                processor.suggest(0, sugg)
        }
    }

    override fun onCommand(input: String) {
        val test = BezierTest.entries.find { it.name.equals(input) }
        if (test != null) {
            scene.mode = test
            return
        }

        val type = BezierType.entries.find { it.name.equals(input) }
        val mode = scene.mode
        val grid = scene.gridVisible
        val nscene = when (type) {
            BezierType.linear -> LinBezierScene()
            BezierType.quadratic -> QuadBezierScene()
            BezierType.cubic -> CubBezierScene()
            BezierType.quadratic_through_points -> QuadBezierThruPtsScene()
            BezierType.cubic_through_points -> CubBezierThruPtsScene()
            null -> scene
        }
        nscene.mode = mode
        nscene.gridVisible = grid

        if (nscene !== scene) {
            scene = nscene
            scene(nscene)
        }
    }
}

fun main() {
    GeometryTest.start(GeoTest::class.java)
}
