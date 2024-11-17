package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3

fun calculateBarycentricCoordinates(p: Vector3, a: Vector3, b: Vector3, c: Vector3): Triple<Float, Float, Float> {
    val v0 = b - a
    val v1 = c - a
    val v2 = p - a
    val d00 = v0.dot(v0)
    val d01 = v0.dot(v1)
    val d11 = v1.dot(v1)
    val d20 = v2.dot(v0)
    val d21 = v2.dot(v1)
    val denom = d00 * d11 - d01 * d01
    val v = (d11 * d20 - d01 * d21) / denom
    val w = (d00 * d21 - d01 * d20) / denom
    val u = 1.0f - v - w
    return Triple(u, v, w)
}
