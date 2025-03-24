package dev.runefox.matlin.geotest

@Suppress("EnumEntryName")
enum class BezierTest {
    none,
    evaluation,
    direction,
    derivative,
    second_derivative,
    integral,
    bbox,
    cut,
    polynomial_parity,
    axis_intersection,
    nonlinearity,
    length,
    line_intersect,
    to_cubic,
    to_quadratics,
    intersect_quadratic,
    intersect_cubic,
    cut_at_extrema,
    nearest_point,
    intersect_circle,
    sample_length,
    inflection,
    max_curvature,
    subtract_curves,
    find_cusp,
    offset_lut,
    curvature,
    arcapprox,
    offset
}
