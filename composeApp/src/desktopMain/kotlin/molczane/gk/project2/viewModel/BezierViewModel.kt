package molczane.gk.project2.viewModel

import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import molczane.gk.project2.model.Material
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.max
import kotlin.math.pow
import molczane.gk.project2.utils.functions.cross
import molczane.gk.project2.utils.functions.times
import java.lang.Math.pow
import kotlin.math.cos
import kotlin.math.sin

class BezierViewModel : ViewModel() {

    // Lighting constants
    private val k_d = Vector3(0.8f, 0.8f, 0.8f) // Diffuse coefficient (example RGB)
    private val k_s = Vector3(0.5f, 0.5f, 0.5f) // Specular coefficient (example RGB)
    private val I_L = Vector3(1.0f, 1.0f, 1.0f) // Light intensity (example RGB)
    private val L_0 = Vector3(0.3f, 0.3f, 0.3f) // Ambient light intensity (example RGB)
    private val n = 32 // Shininess coefficient

    private val _currentTime = MutableStateFlow(0f)
    val currentTime: StateFlow<Float> = _currentTime

    private var animationJob: Job? = null

    init {
        startLightAnimation()
    }

    private fun startLightAnimation() {
        animationJob = viewModelScope.launch {
            var startTime = System.nanoTime()
            while (true) {
                val currentNanoTime = System.nanoTime()
                val elapsedSeconds = (currentNanoTime - startTime) / 1_000_000_000f
                _currentTime.value = elapsedSeconds * 0.5f  // Adjust multiplier to change speed
                delay(16) // Approximately 60 FPS
            }
        }
    }

    // Make sure to cancel the animation when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }

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

    private val _rotationAlpha = MutableStateFlow(0f)
    val rotationAlpha: StateFlow<Float> = _rotationAlpha

    private val _rotationBeta = MutableStateFlow(0f)
    val rotationBeta: StateFlow<Float> = _rotationBeta

    private val _triangulationAccuracy = MutableStateFlow(10)
    val triangulationAccuracy: StateFlow<Int> = _triangulationAccuracy


    private val _mesh = MutableStateFlow(generateBezierMesh())
    val mesh: StateFlow<Mesh> get() = _mesh

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
        val step = 1f / _triangulationAccuracy.value
        for (i in 0 until _triangulationAccuracy.value) {
            for (j in 0 until _triangulationAccuracy.value) {
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

    // Update rotation without regenerating mesh
    fun updateRotation(alpha: Float, beta: Float) {
        _rotationAlpha.value = alpha
        _rotationBeta.value = beta
        // Don't call updateMesh() here as rotation doesn't affect the mesh geometry
    }

    fun updateTriangulation(accuracy: Int) {
        _triangulationAccuracy.value = accuracy
        _mesh.value = generateBezierMesh() // Regenerate mesh with new accuracy
    }

    // Update the calculateLighting function to use currentTime
    fun calculateLighting(triangle: Triangle): Color {
        return calculateLightingInternal(triangle, _currentTime.value)
    }

    // Light position calculation for spiral movement
    private fun calculateLightPosition(time: Float): Vector3 {
        val radius = 5.0f  // Adjust radius of spiral
        val speed = 1.0f   // Adjust speed of movement
        val height = 2.0f  // Height of spiral plane (z=const from requirements)

        return Vector3(
            radius * cos(time * speed),
            radius * sin(time * speed),
            height
        )
    }

    private fun calculateLightingInternal(triangle: Triangle, time: Float): Color {
        // Get light position for current time
        val lightPos = calculateLightPosition(time)

        // Calculate light direction for current point
        val centerPoint = (triangle.vertices[0].position +
                triangle.vertices[1].position +
                triangle.vertices[2].position) / 3.0f
        val L = (lightPos - centerPoint).normalize()

        // Calculate normal vector N for the triangle
        val v0 = triangle.vertices[0].position
        val v1 = triangle.vertices[1].position
        val v2 = triangle.vertices[2].position
        val edge1 = v1 - v0
        val edge2 = v2 - v0
        val N = edge1.cross(edge2).normalize()

        // View vector (V) calculation
        val V = Vector3(0f, 0f, 1f).normalize() // As specified in requirements

        // Calculate R vector (reflection)
        val cosNL = maxOf(N.dot(L), 0f)
        val R = (N * (2f * cosNL) - L).normalize()

        // Calculate lighting components according to the formula:
        // I = kd*IL*Io*cos(kąt(N,L)) + ks*IL*Io*cos^m(kąt(V,R))
        val cosVR = maxOf(V.dot(R), 0f)

        // Use component-wise multiplication for vectors
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

        // Calculate final color
        val resultColor = Vector3(
            (diffuseTerm.x + specularTerm.x),
            (diffuseTerm.y + specularTerm.y),
            (diffuseTerm.z + specularTerm.z)
        )

        // Convert to 0..1 range first, then to 0..255 as specified
        val r = (resultColor.x).coerceIn(0f, 1f) * 255
        val g = (resultColor.y).coerceIn(0f, 1f) * 255
        val b = (resultColor.z).coerceIn(0f, 1f) * 255

        return Color(
            r.toInt().coerceIn(0, 255),
            g.toInt().coerceIn(0, 255),
            b.toInt().coerceIn(0, 255)
        )
    }
//    fun calculateLighting(triangle: Triangle): Color {
//        // Compute the normal vector N for the triangle
//        val v0 = triangle.vertices[0].position
//        val v1 = triangle.vertices[1].position
//        val v2 = triangle.vertices[2].position
//        val edge1 = v1 - v0
//        val edge2 = v2 - v0
//        val normal = edge1.cross(edge2).normalize()
//
//        // Diffuse component
//        val cosTheta = max(normal.dot(lightDirection), 0f)
//        val diffuse = k_d * I_L * cosTheta
//
//        // Specular component
//        val reflection = (normal * (2 * cosTheta) - lightDirection).normalize()
//        val cosPhi = max(reflection.dot(viewDirection), 0f)
//        val specular = k_s * I_L * cosPhi.pow(n)
//
//        // Ambient component
//        val ambient = k_d * L_0
//
//        // Total lighting (diffuse + specular + ambient)
//        val colorVector = ambient + diffuse + specular
//
//        // Clamp RGB values to [0, 1] and convert to Color
//        val r = (colorVector.x).coerceIn(0f, 1f)
//        val g = (colorVector.y).coerceIn(0f, 1f)
//        val b = (colorVector.z).coerceIn(0f, 1f)
//        return Color(r, g, b)
//    }

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

    // Update mesh based on the triangulation accuracy
    fun updateMesh() {
        _mesh.value = generateBezierMesh()
    }
}
