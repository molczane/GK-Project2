package molczane.gk.project2.utils.functions

import androidx.compose.ui.graphics.Color
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import kotlin.math.pow

fun calculateLightingForPoint(
    point: Vector3,
    triangle: Triangle,
    k_d: Vector3 = Vector3(0.8f, 0.8f, 0.8f),
    k_s: Vector3 = Vector3(0.5f, 0.5f, 0.5f),
    I_L: Vector3 = Vector3(1.0f, 1.0f, 1.0f),
    n: Float = 0.2f,
    lightPos: Vector3 = Vector3(0f, 0f, 0f)
): Color {
    // Calculate the triangle normal
    val v0 = triangle.vertices[0].position
    val v1 = triangle.vertices[1].position
    val v2 = triangle.vertices[2].position
    val edge1 = v1 - v0
    val edge2 = v2 - v0
    val normal = edge1.cross(edge2).normalize()

    // Calculate light direction
    val L = (lightPos - point).normalize()
    val cosNL = maxOf(normal.dot(L), 0f)

    // View vector (assuming view is along the z-axis)
    val V = Vector3(0f, 0f, 1f).normalize()
    val R = (normal * (2f * cosNL) - L).normalize()
    val cosVR = maxOf(V.dot(R), 0f)

    // Calculate diffuse and specular terms
    val diffuseTerm = Vector3(
        k_d.x * I_L.x * cosNL,
        k_d.y * I_L.y * cosNL,
        k_d.z * I_L.z * cosNL
    )

    val specularTerm = Vector3(
        k_s.x * I_L.x * cosVR.pow(n),
        k_s.y * I_L.y * cosVR.pow(n),
        k_s.z * I_L.z * cosVR.pow(n)
    )

    // Final color
    val resultColor = Vector3(
        (diffuseTerm.x + specularTerm.x).coerceIn(0f, 1f) * 255,
        (diffuseTerm.y + specularTerm.y).coerceIn(0f, 1f) * 255,
        (diffuseTerm.z + specularTerm.z).coerceIn(0f, 1f) * 255
    )

    return Color(resultColor.x.toInt(), resultColor.y.toInt(), resultColor.z.toInt())
}