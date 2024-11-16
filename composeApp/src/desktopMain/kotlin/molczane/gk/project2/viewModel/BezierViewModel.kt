package molczane.gk.project2.viewModel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.pow
import molczane.gk.project2.utils.functions.cross
import kotlin.math.cos
import kotlin.math.sin

class BezierViewModel : ViewModel() {

    // Lighting constants
    private val k_d = Vector3(0.8f, 0.8f, 0.8f) // Diffuse coefficient (example RGB)
    private val k_s = Vector3(0.5f, 0.5f, 0.5f) // Specular coefficient (example RGB)
    private val I_L = Vector3(1.0f, 1.0f, 1.0f) // Light intensity (example RGB)
    private val L_0 = Vector3(0.3f, 0.3f, 0.3f) // Ambient light intensity (example RGB)
    private val n = 1000 // Shininess coefficient

    private val _currentTime = MutableStateFlow(0f)
    val currentTime: StateFlow<Float> = _currentTime

    private var animationJob: Job? = null
    private var lastFrameTime: Long = 0

    init {
        startLightAnimation()
    }

    private fun startLightAnimation() {
        lastFrameTime = System.nanoTime()
        animationJob = viewModelScope.launch {
            while (true) {
                val currentNanoTime = System.nanoTime()
                val deltaTime = (currentNanoTime - lastFrameTime) / 1_000_000_000f
                _currentTime.value += deltaTime
                lastFrameTime = currentNanoTime
                delay(32)
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

//    private val controlPoints = listOf(
//        Vector3(-5f, -5f, 2f),  Vector3(-1.65f, -5f, -1f), Vector3(1.65f, -5f, 3f),   Vector3(5f, -5f, -2f),
//        Vector3(-5f, -1.65f, -1f), Vector3(-1.65f, -1.65f, 4f), Vector3(1.65f, -1.65f, -3f), Vector3(5f, -1.65f, 2f),
//        Vector3(-5f, 1.65f, 3f), Vector3(-1.65f, 1.65f, -4f), Vector3(1.65f, 1.65f, 5f), Vector3(5f, 1.65f, -1f),
//        Vector3(-5f, 5f, -2f),  Vector3(-1.65f, 5f, 1f),  Vector3(1.65f, 5f, -2f),  Vector3(5f, 5f, 2f)
    //    )
//    private val controlPoints = listOf(
//        Vector3(-5f, -5f, 3f),      Vector3(-1.65f, -5f, 0.5f),   Vector3(1.65f, -5f, 0.5f),   Vector3(5f, -5f, 3f),
//        Vector3(-5f, -1.65f, 0.5f), Vector3(-1.65f, -1.65f, 1f),  Vector3(1.65f, -1.65f, 1f),  Vector3(5f, -1.65f, 0.5f),
//        Vector3(-5f, 1.65f, 0.5f),  Vector3(-1.65f, 1.65f, 1f),   Vector3(1.65f, 1.65f, 1f),   Vector3(5f, 1.65f, 0.5f),
//        Vector3(-5f, 5f, 3f),       Vector3(-1.65f, 5f, 0.5f),    Vector3(1.65f, 5f, 0.5f),    Vector3(5f, 5f, 3f)
//    )



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

    private fun generateBezierMesh(): Mesh {
        val triangles = mutableListOf<Triangle>()
        val step = 1f / _triangulationAccuracy.value

        for (i in 0 until _triangulationAccuracy.value) {
            for (j in 0 until _triangulationAccuracy.value) {
                val u = i * step
                val v = j * step

                // Oblicz pozycje wierzchołków
                val p1 = interpolateBezierSurface(u, v)
                val p2 = interpolateBezierSurface(u + step, v)
                val p3 = interpolateBezierSurface(u, v + step)
                val p4 = interpolateBezierSurface(u + step, v + step)

                // Oblicz wektory styczne dla normalnych
                val tangentU1 = calculateTangentU(u, v)
                val tangentV1 = calculateTangentV(u, v)
                val normal1 = tangentU1.cross(tangentV1).normalize()

                val tangentU2 = calculateTangentU(u + step, v)
                val tangentV2 = calculateTangentV(u + step, v)
                val normal2 = tangentU2.cross(tangentV2).normalize()

                val tangentU3 = calculateTangentU(u, v + step)
                val tangentV3 = calculateTangentV(u, v + step)
                val normal3 = tangentU3.cross(tangentV3).normalize()

                val tangentU4 = calculateTangentU(u + step, v + step)
                val tangentV4 = calculateTangentV(u + step, v + step)
                val normal4 = tangentU4.cross(tangentV4).normalize()

                // Twórz wierzchołki z normalnymi i UV
                val vertex1 = Vertex(position = p1, normal = normal1, uv = Vector3(u, v, 0f))
                val vertex2 = Vertex(position = p2, normal = normal2, uv = Vector3(u + step, v, 0f))
                val vertex3 = Vertex(position = p3, normal = normal3, uv = Vector3(u, v + step, 0f))
                val vertex4 = Vertex(position = p4, normal = normal4, uv = Vector3(u + step, v + step, 0f))

                // Twórz dwa trójkąty dla każdego czworokąta
                triangles.add(Triangle(listOf(vertex1, vertex2, vertex3)))
                triangles.add(Triangle(listOf(vertex2, vertex4, vertex3)))
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

    // Modified light position calculation to match requirements
    private fun calculateLightPosition(time: Float): Vector3 {
        val radius = 2.0f + time * 0.2f  // Spiral grows outward
        val spiralZ = 1.0f  // Constant Z plane as per requirements

        return Vector3(
            radius * cos(time * 2f),  // X coordinate
            radius * sin(time * 2f),  // Y coordinate
            spiralZ                   // Constant Z coordinate
        )
    }

//    fun calculateLightingForPoint(
//        point: Vector3,
//        normal: Vector3,
//        triangle: Triangle,
//        k_d: Vector3 = Vector3(0.8f, 0.8f, 0.8f),
//        k_s: Vector3 = Vector3(0.5f, 0.5f, 0.5f),
//        I_L: Vector3 = Vector3(1.0f, 1.0f, 1.0f),
//        n: Float = 0.2f
//    ): Color {
//        val lightPos = calculateLightPosition(_currentTime.value)
//
//        // Calculate light direction
//        val L = (lightPos - point).normalize()
//        val cosNL = maxOf(normal.dot(L), 0f)
//
//        // View vector (assuming view is along the z-axis)
//        val V = Vector3(0f, 0f, 1f).normalize()
//        val R = (normal * (2f * cosNL) - L).normalize()
//        val cosVR = maxOf(V.dot(R), 0f)
//
//        // Calculate diffuse and specular terms
//        val diffuseTerm = Vector3(
//            k_d.x * I_L.x * cosNL,
//            k_d.y * I_L.y * cosNL,
//            k_d.z * I_L.z * cosNL
//        )
//
//        val specularTerm = Vector3(
//            k_s.x * I_L.x * cosVR.pow(n),
//            k_s.y * I_L.y * cosVR.pow(n),
//            k_s.z * I_L.z * cosVR.pow(n)
//        )
//
//        // Final color
//        val resultColor = Vector3(
//            (diffuseTerm.x + specularTerm.x).coerceIn(0f, 1f) * 255,
//            (diffuseTerm.y + specularTerm.y).coerceIn(0f, 1f) * 255,
//            (diffuseTerm.z + specularTerm.z).coerceIn(0f, 1f) * 255
//        )
//
//        return Color(resultColor.x.toInt(), resultColor.y.toInt(), resultColor.z.toInt())
//    }


   fun calculateLightingForPoint(
    point: Vector3,
    triangle: Triangle,
    k_d: Vector3 = Vector3(0.8f, 0.8f, 0.8f),
    k_s: Vector3 = Vector3(0.5f, 0.5f, 0.5f),
    I_L: Vector3 = Vector3(1.0f, 1.0f, 1.0f),
    n: Float = 0.2f
   ): Color {
       val lightPos = calculateLightPosition(_currentTime.value)

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

    private fun calculateTangentU(u: Float, v: Float): Vector3 {
        val pointsU = (0..3).map { i ->
            val pointsV = (0..3).map { j -> controlPoints[i * 4 + j] }
            interpolateBezier1D(pointsV, v)
        }

        // Oblicz różnice w kierunku U
        var tangent = Vector3(0f, 0f, 0f)
        for (i in 0 until pointsU.size - 1) {
            tangent += (pointsU[i + 1] - pointsU[i]) * 3f * (1 - u).pow(2 - i) * u.pow(i)
        }

        return tangent
    }

    private fun calculateTangentV(u: Float, v: Float): Vector3 {
        val pointsV = (0..3).map { j ->
            val pointsU = (0..3).map { i -> controlPoints[i * 4 + j] }
            interpolateBezier1D(pointsU, u)
        }

        // Oblicz różnice w kierunku V
        var tangent = Vector3(0f, 0f, 0f)
        for (j in 0 until pointsV.size - 1) {
            tangent += (pointsV[j + 1] - pointsV[j]) * 3f * (1 - v).pow(2 - j) * v.pow(j)
        }

        return tangent
    }

    // To be uncommented when the file is ready
    // val points: List<Vertex> = parseBezierSurface("src/points/bezierSurface.txt")
}
