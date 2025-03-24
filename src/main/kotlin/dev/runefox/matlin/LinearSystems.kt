package dev.runefox.matlin

@Suppress("NOTHING_TO_INLINE")
inline fun det(
    mxx: Double, mxy: Double,
    myx: Double, myy: Double,
) = mxx * myy - mxy * myx

@Suppress("NOTHING_TO_INLINE")
inline fun det(
    mxx: Double, mxy: Double, mxz: Double,
    myx: Double, myy: Double, myz: Double,
    mzx: Double, mzy: Double, mzz: Double,
) = mxx * myy * mzz +
    mxy * myz * mzx +
    mxz * myx * mzy -
    mxz * myy * mzx -
    mxy * myx * mzz -
    mxx * myz * mzy

@Suppress("NOTHING_TO_INLINE")
inline fun det(
    mxx: Double, mxy: Double, mxz: Double, mxw: Double,
    myx: Double, myy: Double, myz: Double, myw: Double,
    mzx: Double, mzy: Double, mzz: Double, mzw: Double,
    mwx: Double, mwy: Double, mwz: Double, mww: Double,
) = mxx * det(
    myy, myz, myw,
    mzy, mzz, mzw,
    mwy, mwz, mww,
) - mxy * det(
    myx, myz, myw,
    mzx, mzz, mzw,
    mwx, mwz, mww,
) + mxz * det(
    myx, myy, myw,
    mzx, mzy, mzw,
    mwx, mwy, mww,
) - mxw * det(
    myx, myy, myz,
    mzx, mzy, mzz,
    mwx, mwy, mwz,
)

/**
 * Solve for the equation: `Mv = u`, where `M` is a 2x2 matrix, and `v` and `u` are 2D vectors.
 * ```
 * ⎡ mxx  mxy ⎤⎡ ux ⎤ = ⎡ vx ⎤
 * ⎣ myx  myy ⎦⎣ uy ⎦   ⎣ vy ⎦
 * ```
 */
inline fun solveLinearSystem(
    mxx: Double, mxy: Double,
    myx: Double, myy: Double,
    vx: Double,
    vy: Double,
    result: (
        ux: Double,
        uy: Double
    ) -> Unit
) {
    val idet = 1 / det(
        mxx, mxy,
        myx, myy
    )
    val ixx = +myy * idet
    val ixy = -mxy * idet

    val iyx = -myx * idet
    val iyy = +mxx * idet

    result(
        ixx * vx + ixy * vy,
        iyx * vx + iyy * vy
    )
}

/**
 * Solve for the equation: `Mv = u`, where `M` is a 3x3 matrix, and `v` and `u` are 3D vectors.
 * ```
 * ⎡ mxx  mxy  mxz ⎤⎡ ux ⎤   ⎡ vx ⎤
 * ⎢ myx  myy  myz ⎥⎢ uy ⎥ = ⎢ vy ⎥
 * ⎣ mzx  mzy  mzz ⎦⎣ uz ⎦   ⎣ vz ⎦
 * ```
 */
inline fun solveLinearSystem(
    mxx: Double, mxy: Double, mxz: Double,
    myx: Double, myy: Double, myz: Double,
    mzx: Double, mzy: Double, mzz: Double,
    vx: Double,
    vy: Double,
    vz: Double,
    result: (
        ux: Double,
        uy: Double,
        uz: Double
    ) -> Unit
) {
    val idet = 1 / det(
        mxx, mxy, mxz,
        myx, myy, myz,
        mzx, mzy, mzz,
    )

    val ixx = +det(myy, mzy, /**/ myz, mzz) * idet
    val ixy = -det(mxy, mzy, /**/ mxz, mzz) * idet
    val ixz = +det(mxy, myy, /**/ mxz, myz) * idet

    val iyx = -det(myx, mzx, /**/ myz, mzz) * idet
    val iyy = +det(mxx, mzx, /**/ mxz, mzz) * idet
    val iyz = -det(mxx, myx, /**/ mxz, myz) * idet

    val izx = +det(myx, mzx, /**/ myy, mzy) * idet
    val izy = -det(mxx, mzx, /**/ mxy, mzy) * idet
    val izz = +det(mxx, myx, /**/ mxy, myy) * idet

    result(
        ixx * vx + ixy * vy + ixz * vz,
        iyx * vx + iyy * vy + iyz * vz,
        izx * vx + izy * vy + izz * vz
    )
}

/**
 * Solve for the equation: `Mv = u`, where `M` is a 4x4 matrix, and `v` and `u` are 4D vectors.
 * ```
 * ⎡ mxx  mxy  mxz  mxw ⎤⎡ ux ⎤   ⎡ vx ⎤
 * ⎢ myx  myy  myz  myw ⎥⎢ uy ⎥ = ⎢ vy ⎥
 * ⎢ mzx  mzy  mzz  mzw ⎥⎢ uz ⎥ = ⎢ vz ⎥
 * ⎣ mwx  mwy  mwz  mww ⎦⎣ uw ⎦   ⎣ vw ⎦
 * ```
 */
inline fun solveLinearSystem(
    mxx: Double, mxy: Double, mxz: Double, mxw: Double,
    myx: Double, myy: Double, myz: Double, myw: Double,
    mzx: Double, mzy: Double, mzz: Double, mzw: Double,
    mwx: Double, mwy: Double, mwz: Double, mww: Double,
    vx: Double,
    vy: Double,
    vz: Double,
    vw: Double,
    result: (
        ux: Double,
        uy: Double,
        uz: Double,
        uw: Double
    ) -> Unit
) {
    val idet = 1 / det(
        mxx, mxy, mxz, mxw,
        myx, myy, myz, myw,
        mzx, mzy, mzz, mzw,
        mwx, mwy, mwz, mww,
    )

    val ixx = +det(myy, mzy, mwy, /**/ myz, mzz, mwz, /**/ myw, mzw, mww) * idet
    val ixy = -det(mxy, mzy, mwy, /**/ mxz, mzz, mwz, /**/ mxw, mzw, mww) * idet
    val ixz = +det(mxy, myy, mwy, /**/ mxz, myz, mwz, /**/ mxw, myw, mww) * idet
    val ixw = -det(mxy, myy, mzy, /**/ mxz, myz, mzz, /**/ mxw, myw, mzw) * idet

    val iyx = -det(myx, mzx, mwx, /**/ myz, mzz, mwz, /**/ myw, mzw, mww) * idet
    val iyy = +det(mxx, mzx, mwx, /**/ mxz, mzz, mwz, /**/ mxw, mzw, mww) * idet
    val iyz = -det(mxx, myx, mwx, /**/ mxz, myz, mwz, /**/ mxw, myw, mww) * idet
    val iyw = +det(mxx, myx, mzx, /**/ mxz, myz, mzz, /**/ mxw, myw, mzw) * idet

    val izx = +det(myx, mzx, mwx, /**/ myy, mzy, mwy, /**/ myw, mzw, mww) * idet
    val izy = -det(mxx, mzx, mwx, /**/ mxy, mzy, mwy, /**/ mxw, mzw, mww) * idet
    val izz = +det(mxx, myx, mwx, /**/ mxy, myy, mwy, /**/ mxw, myw, mww) * idet
    val izw = -det(mxx, myx, mzx, /**/ mxy, myy, mzy, /**/ mxw, myw, mzw) * idet

    val iwx = -det(myx, mzx, mwx, /**/ myy, mzy, mwy, /**/ myz, mzz, mwz) * idet
    val iwy = +det(mxx, mzx, mwx, /**/ mxy, mzy, mwy, /**/ mxz, mzz, mwz) * idet
    val iwz = -det(mxx, myx, mwx, /**/ mxy, myy, mwy, /**/ mxz, myz, mwz) * idet
    val iww = +det(mxx, myx, mzx, /**/ mxy, myy, mzy, /**/ mxz, myz, mzz) * idet

    result(
        ixx * vx + ixy * vy + ixz * vz + ixw * vw,
        iyx * vx + iyy * vy + iyz * vz + iyw * vw,
        izx * vx + izy * vy + izz * vz + izw * vw,
        iwx * vx + iwy * vy + iwz * vz + iww * vw
    )
}
