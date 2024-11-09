package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.cos
import kotlin.math.sin

// Sample function to rotate 3D points around X and Y axes
fun rotatePoint(vertex: Vertex, alpha: Float, beta: Float): Vertex {
    val point = vertex.position

    // Rotation around Y-axis (alpha)
    val x1 = point.x * cos(alpha) - point.z * sin(alpha)
    val z1 = point.x * sin(alpha) + point.z * cos(alpha)

    // Rotation around X-axis (beta)
    val y1 = point.y * cos(beta) - z1 * sin(beta)
    val z2 = point.y * sin(beta) + z1 * cos(beta)

    // Return a new Vertex with the rotated position
    return Vertex(Vector3(x1, y1, z2))
}