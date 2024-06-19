package dev.runefox.matlin

/**
 * An axis-aligned bounding box.
 */
data class BoundingBox(
    var minX: Double = 0.0,
    var minY: Double = 0.0,
    var maxX: Double = 0.0,
    var maxY: Double = 0.0
) {
    infix fun overlaps(other: BoundingBox): Boolean {
        return minX <= other.maxX
            && maxX >= other.minX
            && minY <= other.maxY
            && maxY >= other.minY
    }
}
