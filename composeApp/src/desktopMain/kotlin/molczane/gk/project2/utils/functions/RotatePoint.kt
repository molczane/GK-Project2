package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.cos
import kotlin.math.sin

// Function to rotate a vertex around X and Y axes, preserving UV coordinates and rotating the normal
fun rotatePoint(vertex: Vertex, alpha: Float, beta: Float): Vertex {
    val point = vertex.position
    val normal = vertex.normal

    // Rotation around Y-axis (alpha) for position
    val x1 = point.x * cos(alpha) - point.z * sin(alpha)
    val z1 = point.x * sin(alpha) + point.z * cos(alpha)

    // Rotation around X-axis (beta) for position
    val y1 = point.y * cos(beta) - z1 * sin(beta)
    val z2 = point.y * sin(beta) + z1 * cos(beta)

    // Rotate the normal vector similarly to the position
    val nx1 = normal.x * cos(alpha) - normal.z * sin(alpha)
    val nz1 = normal.x * sin(alpha) + normal.z * cos(alpha)

    val ny1 = normal.y * cos(beta) - nz1 * sin(beta)
    val nz2 = normal.y * sin(beta) + nz1 * cos(beta)

    // Return a new Vertex with the rotated position and normal, and the same UV coordinates
    return Vertex(
        position = Vector3(x1, y1, z2),
        normal = Vector3(nx1, ny1, nz2).normalize(), // Normalize the rotated normal
        uv = vertex.uv // UV remains unchanged
    )
}
