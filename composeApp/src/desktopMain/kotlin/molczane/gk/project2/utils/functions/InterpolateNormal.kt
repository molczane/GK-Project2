package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3

fun interpolateNormal(
    point: Vector3, // Punkt, dla którego interpolujemy normalną
    triangle: Triangle
): Vector3 {
    // Wierzchołki trójkąta
    val v0 = triangle.vertices[0].position
    val v1 = triangle.vertices[1].position
    val v2 = triangle.vertices[2].position

    // Wektory normalne wierzchołków trójkąta
    val n0 = triangle.vertices[0].normal
    val n1 = triangle.vertices[1].normal
    val n2 = triangle.vertices[2].normal

    // Oblicz współczynniki barycentryczne dla punktu
    val denom = (v1.y - v2.y) * (v0.x - v2.x) + (v2.x - v1.x) * (v0.y - v2.y)
    val w0 = ((v1.y - v2.y) * (point.x - v2.x) + (v2.x - v1.x) * (point.y - v2.y)) / denom
    val w1 = ((v2.y - v0.y) * (point.x - v2.x) + (v0.x - v2.x) * (point.y - v2.y)) / denom
    val w2 = 1f - w0 - w1

    // Zinterpoluj wektor normalny przy użyciu współczynników barycentrycznych
    val interpolatedNormal = (n0 * w0) + (n1 * w1) + (n2 * w2)

    // Zwróć znormalizowaną normalną
    return interpolatedNormal.normalize()
}
