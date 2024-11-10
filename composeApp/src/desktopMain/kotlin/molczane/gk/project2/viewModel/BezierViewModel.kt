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

    // Define control points for the Bezier surface with values in the range -5 to 5
    private val controlPoints = listOf(
        Vector3(-5f, -5f, 0f), Vector3(-1.65f, -5f, 1f), Vector3(1.65f, -5f, 0.5f), Vector3(5f, -5f, 0f),
        Vector3(-5f, -1.65f, 1.5f), Vector3(-1.65f, -1.65f, 2.5f), Vector3(1.65f, -1.65f, 2f), Vector3(5f, -1.65f, 1f),
        Vector3(-5f, 1.65f, 2f), Vector3(-1.65f, 1.65f, 3f), Vector3(1.65f, 1.65f, 2.5f), Vector3(5f, 1.65f, 1.5f),
        Vector3(-5f, 5f, 0f), Vector3(-1.65f, 5f, 1f), Vector3(1.65f, 5f, 0.5f), Vector3(5f, 5f, 0f)
    )


    var rotationAlpha by mutableStateOf(0f)
    var rotationBeta by mutableStateOf(0f)
    var triangulationAccuracy by mutableStateOf(10) // Accuracy of the triangulation

    private val _mesh = MutableStateFlow(generateBezierMesh())
    val mesh: StateFlow<Mesh> get() = _mesh

    // To be uncommented when the file is ready
    // val points: List<Vertex> = parseBezierSurface("src/points/bezierSurface.txt")
    // Sample function to create a basic mesh for testing purposes
    // Generate a sample mesh with realistic Vector3 vertices for testing
    private fun generateSampleMesh(): Mesh {
        // Define sample vertices using Vector3 coordinates
        val vertices = listOf(
            Vertex(Vector3(-0.5f, -0.5f, 0f)),
            Vertex(Vector3(0.5f, -0.5f, 1f)),
            Vertex(Vector3(0f, 0.5f, 2f))
        )
        // Create a list of triangles using these vertices
        val triangles = listOf(Triangle(vertices))
        return Mesh(triangles)
    }

    // Function to generate points on the Bezier surface
    private fun interpolateBezierSurface(u: Float, v: Float): Vector3 {
        val pointsU = (0..3).map { i ->
            val pointsV = (0..3).map { j -> controlPoints[i * 4 + j] }
            interpolateBezier1D(pointsV, v)
        }
        return interpolateBezier1D(pointsU, u)
    }

    // 1D Bezier interpolation
    private fun interpolateBezier1D(points: List<Vector3>, t: Float): Vector3 {
        var tempPoints = points
        for (i in 1 until points.size) {
            tempPoints = tempPoints.windowed(2, 1) { (p1, p2) ->
                p1 * (1 - t) + p2 * t
            }
        }
        return tempPoints[0]
    }

    // Generate a mesh for the Bezier surface based on the control points
    private fun generateBezierMesh(): Mesh {
        val triangles = mutableListOf<Triangle>()
        val step = 1f / triangulationAccuracy
        for (i in 0 until triangulationAccuracy) {
            for (j in 0 until triangulationAccuracy) {
                val u = i * step
                val v = j * step
                val p1 = Vertex(interpolateBezierSurface(u, v))
                val p2 = Vertex(interpolateBezierSurface(u + step, v))
                val p3 = Vertex(interpolateBezierSurface(u, v + step))
                val p4 = Vertex(interpolateBezierSurface(u + step, v + step))

                // Create two triangles for each quad
                triangles.add(Triangle(listOf(p1, p2, p3)))
                triangles.add(Triangle(listOf(p2, p4, p3)))
            }
        }
        return Mesh(triangles)
    }

    // Update mesh based on the triangulation accuracy
    fun updateMesh() {
        _mesh.value = generateBezierMesh()
    }

    // Method to update rotation based on user input
    fun updateRotation(alpha: Float, beta: Float) {
        rotationAlpha = alpha
        rotationBeta = beta
        updateMesh() // Recompute mesh based on new rotation values
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
