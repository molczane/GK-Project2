package molczane.gk.project2.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.max
import kotlin.math.pow

class BezierViewModel : ViewModel() {

    // Lighting constants
    private val k_d = Vector3(0.8f, 0.8f, 0.8f) // Diffuse coefficient (example RGB)
    private val k_s = Vector3(0.5f, 0.5f, 0.5f) // Specular coefficient (example RGB)
    private val I_L = Vector3(1.0f, 1.0f, 1.0f) // Light intensity (example RGB)
    private val L_0 = Vector3(0.3f, 0.3f, 0.3f) // Ambient light intensity (example RGB)
    private val n = 32 // Shininess coefficient

    // Light direction vector (e.g., from above and to the right)
    private val lightDirection = Vector3(1.0f, 1.0f, 1.0f).normalize()

    // Camera/view direction vector (assuming it's from the z-axis)
    private val viewDirection = Vector3(0.0f, 0.0f, 1.0f).normalize()


    private val _mesh = MutableStateFlow(generateSampleMesh())
    val mesh: StateFlow<Mesh> get() = _mesh

    var rotationAlpha by mutableStateOf(0f)
    var rotationBeta by mutableStateOf(0f)
    var triangulationAccuracy by mutableStateOf(3) // Placeholder for triangulation accuracy slider

    // To be uncommented when the file is ready
    // val points: List<Vertex> = parseBezierSurface("src/points/bezierSurface.txt")
    // Sample function to create a basic mesh for testing purposes
    // Generate a sample mesh with realistic Vector3 vertices for testing
    private fun generateSampleMesh(): Mesh {
        // Define sample vertices using Vector3 coordinates
        val vertices = listOf(
            Vertex(Vector3(-0.5f, -0.5f, 0f)),
            Vertex(Vector3(0.5f, -0.5f, 0f)),
            Vertex(Vector3(0f, 0.5f, 0f))
        )
        // Create a list of triangles using these vertices
        val triangles = listOf(Triangle(vertices))
        return Mesh(triangles)
    }

    // Method to update rotation based on user input
    fun updateRotation(alpha: Float, beta: Float) {
        rotationAlpha = alpha
        rotationBeta = beta
        updateMesh() // Recompute mesh based on new rotation values
    }

    // Placeholder for updating the mesh
    private fun updateMesh() {
        // Implement rotation transformation and other calculations here
    }

    fun calculateLighting(triangle: Triangle): Color {
        // Compute the normal vector N for the triangle
        val v0 = triangle.vertices[0].position
        val v1 = triangle.vertices[1].position
        val v2 = triangle.vertices[2].position
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        val normal = edge1.cross(edge2).normalize()

        // Diffuse component
        val cosTheta = max(normal.dot(lightDirection), 0f)
        val diffuse = k_d * I_L * cosTheta

        // Specular component
        val reflection = (normal * (2 * cosTheta) - lightDirection).normalize()
        val cosPhi = max(reflection.dot(viewDirection), 0f)
        val specular = k_s * I_L * cosPhi.pow(n)

        // Ambient component
        val ambient = k_d * L_0

        // Total lighting (diffuse + specular + ambient)
        val colorVector = ambient + diffuse + specular

        // Clamp RGB values to [0, 1] and convert to Color
        val r = (colorVector.x).coerceIn(0f, 1f)
        val g = (colorVector.y).coerceIn(0f, 1f)
        val b = (colorVector.z).coerceIn(0f, 1f)
        return Color(r, g, b)
    }

    // Extension functions for vector operations
    private operator fun Vector3.times(other: Vector3) = Vector3(x * other.x, y * other.y, z * other.z)
    private fun Vector3.cross(other: Vector3) = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}
