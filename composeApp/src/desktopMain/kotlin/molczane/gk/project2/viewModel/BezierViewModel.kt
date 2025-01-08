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
import molczane.gk.project2.utils.functions.loadNormalMap
import molczane.gk.project2.utils.functions.interpolateNormal
import molczane.gk.project2.utils.functions.loadTexture
import molczane.gk.project2.utils.functions.times
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

class BezierViewModel : ViewModel() {

    private val I_L: Vector3 = Vector3(1f, 1f, 1f)
    private var I_0: Vector3 = Vector3(3f, 3f, 3f)

    private val _k_d = MutableStateFlow(0.5f)
    val k_d: StateFlow<Float> = _k_d

    /* LAB PART */
    private val _isRedLightTurnedOn = MutableStateFlow(true)
    val isRedLightTurnedOn: StateFlow<Boolean> = _isRedLightTurnedOn

    private val _isBlueLightTurnedOn = MutableStateFlow(true)
    val isBlueLightTurnedOn: StateFlow<Boolean> = _isBlueLightTurnedOn

    private val _isGreenLightTurnedOn = MutableStateFlow(true)
    val isGreenLightTurnedOn: StateFlow<Boolean> = _isGreenLightTurnedOn

    private var I_R: Vector3 = Vector3(1f, 0f, 0f)
    private var I_G: Vector3 = Vector3(0f, 1f, 0f)
    private var I_B: Vector3 = Vector3(0f, 0f, 1f)

    private val lightPosRed = Vector3(5f, 5f, 3f)
    private val lightPosBlue = Vector3(-5f, 5f, 3f)
    private val lightPosGreen = Vector3(-5f, -5f, 3f)

    // Dodaj kierunki reflektorów
    private val spotlightDirRed = (Vector3(0f, 0f, 0f) - lightPosRed).normalize()
    private val spotlightDirBlue = (Vector3(0f, 0f, 0f) - lightPosBlue).normalize()
    private val spotlightDirGreen = (Vector3(0f, 0f, 0f) - lightPosGreen).normalize()

    // Funkcja skupienia reflektora
    private fun spotlightIntensity(L: Vector3, spotlightDir: Vector3, cutoffAngle: Float): Float {
        spotlightDir.normalize()
        val cosTheta = L.dot(spotlightDir.normalize())
        return if (cosTheta >= cos(cutoffAngle)) {
            cosTheta.pow(1) // Możesz dopasować wykładnik, aby kontrolować "ostrość" skupienia
        } else {
            0f
        }
    }
    /* LAB PART */

    private val _k_s = MutableStateFlow(0.5f)
    val k_s: StateFlow<Float> = _k_s

    private val _m = MutableStateFlow(100f)
    val m: StateFlow<Float> = _m

    private val _currentTime = MutableStateFlow(0f)
    val currentTime: StateFlow<Float> = _currentTime

    private var animationJob: Job? = null
    private var lastFrameTime: Long = 0

    private val _isLightAnimationRunning = MutableStateFlow(false)
    val isLightAnimationRunning: StateFlow<Boolean> = _isLightAnimationRunning

    private val _isTextureTurnedOn = MutableStateFlow(false)
    val isTextureTurnedOn: StateFlow<Boolean> = _isTextureTurnedOn

    private val _isNormalMappingTurnedOn = MutableStateFlow(false)
    val isNormalMappingTurnedOn: StateFlow<Boolean> = _isNormalMappingTurnedOn

    private val _lightPos = MutableStateFlow(Vector3(0f, 0f, 0f))
    val lightPos: StateFlow<Vector3> = _lightPos

    var normalMap : Array<Array<Vector3>>

    var texture: Array<Array<Color>>

    val controlPoints: List<Vector3>

    init {
        if(_isLightAnimationRunning.value) {
            startLightAnimation()
        }
        normalMap = invertNormals(loadNormalMap("src/normal-maps/brick_normalmap.png"))
        texture = loadTexture("src/textures/people-texture.png")
        controlPoints = parseBezierSurface("src/points/bezier_surface_points.txt")
    }

    private fun startLightAnimation() {
        lastFrameTime = System.nanoTime()
        animationJob?.cancel() // Anuluj poprzednią animację, jeśli istnieje
        animationJob = viewModelScope.launch {
            while (true) {
                val currentNanoTime = System.nanoTime()
                val deltaTime = (currentNanoTime - lastFrameTime) / 1_000_000_000f
                _currentTime.value += deltaTime
                lastFrameTime = currentNanoTime
                _lightPos.value = calculateLightPosition(_currentTime.value)
                delay(128)
            }
        }
    }

    fun toggleLightAnimation() {
        _isLightAnimationRunning.value = !_isLightAnimationRunning.value
        if (_isLightAnimationRunning.value) {
            startLightAnimation()
        } else {
            animationJob?.cancel()
        }
    }

    // Modified light position calculation to match requirements
    private fun calculateLightPosition(time: Float): Vector3 {
        val radius = 1.0f + time * 0.1f  // Spiral grows outward
        val spiralZ = -3f  // Constant Z plane as per requirements

        return Vector3(
            radius * cos(time * 2f),  // X coordinate
            radius * sin(time * 2f),  // Y coordinate
            spiralZ                   // Constant Z coordinate
        )
    }

    // Make sure to cancel the animation when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
    }

    // Define control points for the Bezier surface with values in the range -5 to 5
//    private val controlPoints = listOf(
//        Vector3(-5f, -5f, 0f), Vector3(-1.65f, -5f, 1f), Vector3(1.65f, -5f, 0.5f), Vector3(5f, -5f, 0f),
//        Vector3(-5f, -1.65f, 1.5f), Vector3(-1.65f, -1.65f, 2.5f), Vector3(1.65f, -1.65f, 2f), Vector3(5f, -1.65f, 1f),
//        Vector3(-5f, 1.65f, 2f), Vector3(-1.65f, 1.65f, 3f), Vector3(1.65f, 1.65f, 2.5f), Vector3(5f, 1.65f, 1.5f),
//        Vector3(-5f, 5f, 0f), Vector3(-1.65f, 5f, 1f), Vector3(1.65f, 5f, 0.5f), Vector3(5f, 5f, 0f)
//    )

//    private val controlPoints = listOf(
//        Vector3(-5f, -5f, 2f),  Vector3(-1.65f, -5f, -1f), Vector3(1.65f, -5f, 3f),   Vector3(5f, -5f, -2f),
//        Vector3(-5f, -1.65f, -1f), Vector3(-1.65f, -1.65f, 4f), Vector3(1.65f, -1.65f, -3f), Vector3(5f, -1.65f, 2f),
//        Vector3(-5f, 1.65f, 3f), Vector3(-1.65f, 1.65f, -4f), Vector3(1.65f, 1.65f, 5f), Vector3(5f, 1.65f, -1f),
//        Vector3(-5f, 5f, -2f),  Vector3(-1.65f, 5f, 1f),  Vector3(1.65f, 5f, -2f),  Vector3(5f, 5f, 2f)
//        )
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


    private val _mesh = MutableStateFlow(Mesh(emptyList()))
    val mesh: StateFlow<Mesh> get() = _mesh

    init {
        viewModelScope.launch {
            _mesh.value = generateBezierMesh()
        }
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

    private fun generateBezierMesh(): Mesh {
        if(_isNormalMappingTurnedOn.value) {
            return generateBezierMeshWithMap()
        }
        else {
            return generateBezierMeshNormal()
        }
    }

    // normals calculated and from map combined
    private fun generateBezierMeshWithMap(): Mesh {
        val triangles = mutableListOf<Triangle>()
        val step = 1f / _triangulationAccuracy.value

        for (i in 0 until _triangulationAccuracy.value) {
            for (j in 0 until _triangulationAccuracy.value) {
                val u = i * step
                val v = j * step

                // Compute vertex positions
                val p1 = interpolateBezierSurface(u, v)
                val p2 = interpolateBezierSurface(u + step, v)
                val p3 = interpolateBezierSurface(u, v + step)
                val p4 = interpolateBezierSurface(u + step, v + step)

                // Interpolate normals from the normal map
                val normal1_1 = interpolateNormalFromMap(u, v, normalMap)
                val normal2_1 = interpolateNormalFromMap(u + step, v, normalMap)
                val normal3_1 = interpolateNormalFromMap(u, v + step, normalMap)
                val normal4_1 = interpolateNormalFromMap(u + step, v + step, normalMap)

                // Oblicz wektory styczne dla normalnych
                val tangentU1 = calculateTangentU(u, v)
                val tangentV1 = calculateTangentV(u, v)
                val normal1_2 = tangentU1.cross(tangentV1).normalize()

                val tangentU2 = calculateTangentU(u + step, v)
                val tangentV2 = calculateTangentV(u + step, v)
                val normal2_2 = tangentU2.cross(tangentV2).normalize()

                val tangentU3 = calculateTangentU(u, v + step)
                val tangentV3 = calculateTangentV(u, v + step)
                val normal3_2 = tangentU3.cross(tangentV3).normalize()

                val tangentU4 = calculateTangentU(u + step, v + step)
                val tangentV4 = calculateTangentV(u + step, v + step)
                val normal4_2 = tangentU4.cross(tangentV4).normalize()

                val normal1 = (normal1_1 + normal1_2).normalize()
                val normal2 = (normal2_1 + normal2_2).normalize()
                val normal3 = (normal3_1 + normal3_2).normalize()
                val normal4 = (normal4_1 + normal4_2).normalize()

                // Create vertices with interpolated normals
                val vertex1 = Vertex(position = p1, normal = normal1, uv = Vector3(u, v, 0f))
                val vertex2 = Vertex(position = p2, normal = normal2, uv = Vector3(u + step, v, 0f))
                val vertex3 = Vertex(position = p3, normal = normal3, uv = Vector3(u, v + step, 0f))
                val vertex4 = Vertex(position = p4, normal = normal4, uv = Vector3(u + step, v + step, 0f))

                // Create two triangles for each quad
                triangles.add(Triangle(listOf(vertex1, vertex2, vertex3)))
                triangles.add(Triangle(listOf(vertex2, vertex4, vertex3)))
            }
        }

        return Mesh(triangles)
    }

    // traditional
    private fun generateBezierMeshNormal(): Mesh {
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
//        viewModelScope.launch {
//            _mesh.value = generateBezierMesh()
//        }
    }

    fun updateTriangulation(accuracy: Int) {
        _triangulationAccuracy.value = accuracy

        viewModelScope.launch {
            _mesh.value = generateBezierMesh()
        }
//        _triangulationAccuracy.value = accuracy
//        _mesh.value = generateBezierMesh() // Regenerate mesh with new accuracy
    }


    // wersja z reflektorami
    // Zmodyfikowana funkcja obliczania oświetlenia
    fun calculateLightingForPoint(
        point: Vector3,
        triangle: Triangle,
    ): Color {
        val interpolatedNormal = interpolateNormal(point, triangle)
        val N = interpolatedNormal.normalize()

        // Kierunki światła dla każdego reflektora
//        val L_red = (lightPosRed - point).normalize()
//        val L_blue = (lightPosBlue - point).normalize()
//        val L_green = (lightPosGreen - point).normalize()

        val L_red = (point - lightPosRed).normalize()
        val L_blue = (point - lightPosBlue).normalize()
        val L_green = (point - lightPosGreen).normalize()

        val V = Vector3(0f, 0f, -5f).normalize()

        // Skupienie dla każdego reflektora
        var intensityRed = spotlightIntensity(L_red, spotlightDirRed, Math.toRadians(30.0).toFloat())
        var intensityBlue = spotlightIntensity(L_blue, spotlightDirBlue, Math.toRadians(30.0).toFloat())
        var intensityGreen = spotlightIntensity(L_green, spotlightDirGreen, Math.toRadians(30.0).toFloat())

        if(intensityRed > 0) ;
            //println("Red intensity: $intensityRed")

        // Rozproszone i zwierciadlane składowe
        val cosNL_red = maxOf(N.dot(L_red), 0f)
        val cosNL_blue = maxOf(N.dot(L_blue), 0f)
        val cosNL_green = maxOf(N.dot(L_green), 0f)

        val R_red = (N * (2f * N.dot(L_red)) - L_red).normalize()
        val R_blue = (N * (2f * N.dot(L_blue)) - L_blue).normalize()
        val R_green = (N * (2f * N.dot(L_green)) - L_green).normalize()

        val cosVR_red = maxOf(V.dot(R_red), 0f)
        val cosVR_blue = maxOf(V.dot(R_blue), 0f)
        val cosVR_green = maxOf(V.dot(R_green), 0f)

        val diffuseRed = I_0 * k_d.value * I_R * cosNL_red * intensityRed
        val diffuseBlue = I_0 * k_d.value * I_B * cosNL_blue * intensityBlue
        val diffuseGreen = I_0 * k_d.value * I_G * cosNL_green * intensityGreen

        val specularRed = I_0 * k_s.value * I_R * cosVR_red.pow(m.value) * intensityRed
        val specularBlue = I_0 * k_s.value * I_B * cosVR_blue.pow(m.value) * intensityBlue
        val specularGreen = I_0 * k_s.value * I_G * cosVR_green.pow(m.value) * intensityGreen

        var resultColor = Vector3(0f, 0f, 0f)

        if(_isRedLightTurnedOn.value) {
            resultColor += diffuseRed + specularRed
        }
        if(_isBlueLightTurnedOn.value) {
            resultColor += diffuseBlue + specularBlue
        }
        if(_isGreenLightTurnedOn.value) {
            resultColor += diffuseGreen + specularGreen
        }
        //val resultColor = diffuseRed + specularRed + diffuseBlue + specularBlue + diffuseGreen + specularGreen

        // resultColor += Vector3(triangle.baseColor.red/256, triangle.baseColor.green/256, triangle.baseColor.blue/256)

        // Add triangle's base color to the lighting
        val baseColor = Vector3(
            triangle.baseColor.red,
            triangle.baseColor.green,
            triangle.baseColor.blue
        )
        resultColor *= baseColor // Modulate the result with the base color

        val colorFromWhite = calculateLightingForPointNormal(point, triangle)

        return Color(resultColor.x + colorFromWhite.red, resultColor.y + colorFromWhite.green, resultColor.z + colorFromWhite.blue)
    }


    fun calculateLightingForPointNormal(
        point: Vector3,
        triangle: Triangle,
    ): Color {
        // pozycja światła
        val lightPos = _lightPos.value

        val interpolatedNormal = interpolateNormal(point, triangle)

        // Normalizuj interpolowaną normalną
        val N = interpolatedNormal.normalize()

        // Calculate light direction
        val L = (lightPos - point).normalize() // Kierunek światła

        // Wektor widzenia (zakładamy, że obserwator znajduje się w pozycji kamery)
        val V = Vector3(0f, 0f, -5f).normalize()

        // Wektor odbicia
        val R = (N * (2f * N.dot(L)) - L).normalize()

        // Kąt między N i L (rozproszone oświetlenie)
        val cosNL = maxOf(N.dot(L), 0f)

        // Kąt między V i R (zwierciadlane oświetlenie)
        val cosVR = maxOf(V.dot(R), 0f)

        // Oblicz rozproszone i zwierciadlane składowe oświetlenia
        val diffuse = I_0 * k_d.value * I_L * cosNL
        val specular = I_0 * k_s.value * I_L * cosVR.pow(m.value)

        // Sumuj składowe
        val resultColor = diffuse + specular

        // Ogranicz wartości do zakresu [0, 1], a następnie przeskaluj na [0, 255]
        val r = (resultColor.x.coerceIn(0f, 1f) * 255).toInt()
        val g = (resultColor.y.coerceIn(0f, 1f) * 255).toInt()
        val b = (resultColor.z.coerceIn(0f, 1f) * 255).toInt()

        return Color(r, g, b)
    }

    fun calculateLightingForPointPyramid(
        point: Vector3,
        triangle: Triangle
    ): Color {
        // Light position
        val lightPos = _lightPos.value

        // Interpolated normal using barycentric coordinates
        val interpolatedNormal = interpolateNormal(point, triangle)
        val N = interpolatedNormal.normalize()

        // Light direction
        val L = (lightPos - point).normalize()

        // View direction (assuming the observer is at a fixed camera position)
        val V = Vector3(0f, 0f, -5f).normalize()

        // Reflection vector
        val R = (N * (2f * N.dot(L)) - L).normalize()

        // Diffuse and specular components
        val cosNL = maxOf(N.dot(L), 0f)
        val cosVR = maxOf(V.dot(R), 0f)

        val diffuse = I_0 * k_d.value * I_L * cosNL
        val specular = I_0 * k_s.value * I_L * cosVR.pow(m.value)

        // Interpolate UV coordinates using barycentric interpolation
        val interpolatedUV = interpolateUV(point, triangle.vertices)

        // Map UV coordinates to texture coordinates
        val texWidth = texture.size
        val texHeight = texture[0].size
        val texU = (interpolatedUV.x * (texWidth - 1)).toInt().coerceIn(0, texWidth - 1)
        val texV = (interpolatedUV.y * (texHeight - 1)).toInt().coerceIn(0, texHeight - 1)

        // Sample texture color
        val textureColor = triangle.baseColor

        // Combine texture color with lighting
        val resultColor = Vector3(
            textureColor.red * (diffuse.x + specular.x),
            textureColor.green * (diffuse.y + specular.y),
            textureColor.blue * (diffuse.z + specular.z)
        )

        // Clamp values to [0, 1] and convert to 255 scale
        val r = (resultColor.x.coerceIn(0f, 1f) * 255).toInt()
        val g = (resultColor.y.coerceIn(0f, 1f) * 255).toInt()
        val b = (resultColor.z.coerceIn(0f, 1f) * 255).toInt()

        return Color(r, g, b)
    }

    fun calculateLightingForPointWithTexture(
        point: Vector3,
        triangle: Triangle
    ): Color {
        // Light position
        val lightPos = _lightPos.value

        // Interpolated normal using barycentric coordinates
        val interpolatedNormal = interpolateNormal(point, triangle)
        val N = interpolatedNormal.normalize()

        // Light direction
        val L = (lightPos - point).normalize()

        // View direction (assuming the observer is at a fixed camera position)
        val V = Vector3(0f, 0f, -5f).normalize()

        // Reflection vector
        val R = (N * (2f * N.dot(L)) - L).normalize()

        // Diffuse and specular components
        val cosNL = maxOf(N.dot(L), 0f)
        val cosVR = maxOf(V.dot(R), 0f)

        val diffuse = I_0 * k_d.value * I_L * cosNL
        val specular = I_0 * k_s.value * I_L * cosVR.pow(m.value)

        // Interpolate UV coordinates using barycentric interpolation
        val interpolatedUV = interpolateUV(point, triangle.vertices)

        // Map UV coordinates to texture coordinates
        val texWidth = texture.size
        val texHeight = texture[0].size
        val texU = (interpolatedUV.x * (texWidth - 1)).toInt().coerceIn(0, texWidth - 1)
        val texV = (interpolatedUV.y * (texHeight - 1)).toInt().coerceIn(0, texHeight - 1)

        // Sample texture color
        val textureColor = texture[texU][texV]

        // Combine texture color with lighting
        val resultColor = Vector3(
            textureColor.red * (diffuse.x + specular.x),
            textureColor.green * (diffuse.y + specular.y),
            textureColor.blue * (diffuse.z + specular.z)
        )

        // Clamp values to [0, 1] and convert to 255 scale
        val r = (resultColor.x.coerceIn(0f, 1f) * 255).toInt()
        val g = (resultColor.y.coerceIn(0f, 1f) * 255).toInt()
        val b = (resultColor.z.coerceIn(0f, 1f) * 255).toInt()

        return Color(r, g, b)
    }

    // Interpolate UV coordinates
    fun interpolateUV(point: Vector3, vertices: List<Vertex>): Vector3 {
        val v0 = vertices[0]
        val v1 = vertices[1]
        val v2 = vertices[2]

        val denom = ((v1.position.y - v2.position.y) * (v0.position.x - v2.position.x) +
                (v2.position.x - v1.position.x) * (v0.position.y - v2.position.y))

        val w0 = ((v1.position.y - v2.position.y) * (point.x - v2.position.x) +
                (v2.position.x - v1.position.x) * (point.y - v2.position.y)) / denom

        val w1 = ((v2.position.y - v0.position.y) * (point.x - v2.position.x) +
                (v0.position.x - v2.position.x) * (point.y - v2.position.y)) / denom

        val w2 = 1f - w0 - w1

        return v0.uv * w0 + v1.uv * w1 + v2.uv * w2
    }

    private fun calculateTangentU(u: Float, v: Float): Vector3 {
        val pointsU = (0..3).map { i ->
            val pointsV = (0..3).map { j -> controlPoints[i * 4 + j] }
            interpolateBezier1D(pointsV, v)
        }

        return (pointsU[1] - pointsU[0]) * (3 * (1 - u).pow(2)) +
                (pointsU[2] - pointsU[1]) * (6 * (1 - u) * u) +
                (pointsU[3] - pointsU[2]) * (3 * u.pow(2))
    }

    private fun calculateTangentV(u: Float, v: Float): Vector3 {
        val pointsV = (0..3).map { j ->
            val pointsU = (0..3).map { i -> controlPoints[i * 4 + j] }
            interpolateBezier1D(pointsU, u)
        }

        return (pointsV[1] - pointsV[0]) * (3 * (1 - v).pow(2)) +
                (pointsV[2] - pointsV[1]) * (6 * (1 - v) * v) +
                (pointsV[3] - pointsV[2]) * (3 * v.pow(2))
    }

    fun updateK_d(it: Float) {
        _k_d.value = it
    }

    fun updateK_s(it: Float) {
        _k_s.value = it
    }

    fun updateM(it: Float) {
        _m.value = it
    }

    fun updateColor(it: Vector3) {
        I_0 = it
    }

    fun updateNormalMapping(it: Boolean, file: String?) {

        if(file != null) {
            normalMap = invertNormals(loadNormalMap(file))
            _isNormalMappingTurnedOn.value = it
        }
        else {
            _isNormalMappingTurnedOn.value = it
        }
        viewModelScope.launch {
            _mesh.value = generateBezierMesh()
        }
    }

    fun updateTexture(it: Boolean, file: String?) {
        if(file != null) {
            texture = loadTexture(file)
            _isTextureTurnedOn.value = it
        }
        else {
            _isTextureTurnedOn.value = it
        }
    }

    fun interpolateNormalFromMap(
        u: Float,                 // Normalized U-coordinate [0, 1]
        v: Float,                 // Normalized V-coordinate [0, 1]
        normalMap: Array<Array<Vector3>> // The dense normal map
    ): Vector3 {
        val mapHeight = normalMap.size
        val mapWidth = normalMap[0].size

        // Scale U, V to normal map indices
        val x = u * (mapWidth - 1)
        val y = v * (mapHeight - 1)

        val x0 = x.toInt().coerceIn(0, mapWidth - 1)
        val x1 = (x0 + 1).coerceIn(0, mapWidth - 1)
        val y0 = y.toInt().coerceIn(0, mapHeight - 1)
        val y1 = (y0 + 1).coerceIn(0, mapHeight - 1)

        // Bilinear interpolation
        val topLeft = normalMap[y0][x0]
        val topRight = normalMap[y0][x1]
        val bottomLeft = normalMap[y1][x0]
        val bottomRight = normalMap[y1][x1]

        val tx = x - x0
        val ty = y - y0

        val top = topLeft * (1 - tx) + topRight * tx
        val bottom = bottomLeft * (1 - tx) + bottomRight * tx

        return (top * (1 - ty) + bottom * ty).normalize()
    }


    fun invertNormals(normalMap: Array<Array<Vector3>>): Array<Array<Vector3>> {
        return normalMap.map { row ->
            row.map { normal ->
                normal * -1f // Obrót wektora w przeciwną stronę
            }.toTypedArray()
        }.toTypedArray()
    }

    fun updateRedReflector(it: Boolean) {
        _isRedLightTurnedOn.value = it
    }

    fun updateGreenReflector(it: Boolean) {
        _isGreenLightTurnedOn.value = it
    }

    fun updateBlueReflector(it: Boolean) {
        _isBlueLightTurnedOn.value = it
    }

    fun generatePyramidMesh(): Mesh {
        val baseCenter = Vector3(0f, 0f, -1f) // Center of the pyramid's base (partially below the surface)
        val height = 3f                      // Height of the pyramid
        val halfBaseSize = 2f                // Half the length of the base square's side

        // Define the pyramid's vertices
        val topVertex = Vertex(
            position = Vector3(0f, 0f, height),
            normal = Vector3(0f, 0f, 1f),
            uv = Vector3(0.5f, 0.5f, 0f) // UV coordinates for the top point
        )

        val baseVertices = listOf(
            Vertex(
                position = baseCenter + Vector3(-halfBaseSize, -halfBaseSize, 0f),
                normal = Vector3(0f, 0f, -1f), // Normal facing downward
                uv = Vector3(0f, 0f, 0f)       // UV for bottom-left
            ),
            Vertex(
                position = baseCenter + Vector3(halfBaseSize, -halfBaseSize, 0f),
                normal = Vector3(0f, 0f, -1f), // Normal facing downward
                uv = Vector3(1f, 0f, 0f)       // UV for bottom-right
            ),
            Vertex(
                position = baseCenter + Vector3(halfBaseSize, halfBaseSize, 0f),
                normal = Vector3(0f, 0f, -1f), // Normal facing downward
                uv = Vector3(1f, 1f, 0f)       // UV for top-right
            ),
            Vertex(
                position = baseCenter + Vector3(-halfBaseSize, halfBaseSize, 0f),
                normal = Vector3(0f, 0f, -1f), // Normal facing downward
                uv = Vector3(0f, 1f, 0f)       // UV for top-left
            )
        )

        // Define the base as two triangles
        val baseTriangles = listOf(
            Triangle(
                listOf(baseVertices[0], baseVertices[1], baseVertices[2]),
                baseColor = Color.Cyan
            ),
            Triangle(
                listOf(baseVertices[0], baseVertices[2], baseVertices[3]),
                baseColor = Color.Cyan
            )
        )

        // Define the walls as four triangles
        val wallTriangles = listOf(
            Triangle(
                listOf(
                    baseVertices[0],
                    baseVertices[1],
                    topVertex.copy(normal = (baseVertices[1].position - topVertex.position).cross(baseVertices[0].position - topVertex.position).normalize())
                ),
                baseColor = Color.Magenta
            ),
            Triangle(
                listOf(
                    baseVertices[1],
                    baseVertices[2],
                    topVertex.copy(normal = (baseVertices[2].position - topVertex.position).cross(baseVertices[1].position - topVertex.position).normalize())
                ),
                baseColor = Color.Yellow
            ),
            Triangle(
                listOf(
                    baseVertices[2],
                    baseVertices[3],
                    topVertex.copy(normal = (baseVertices[3].position - topVertex.position).cross(baseVertices[2].position - topVertex.position).normalize())
                ),
                baseColor = Color.Green
            ),
            Triangle(
                listOf(
                    baseVertices[3],
                    baseVertices[0],
                    topVertex.copy(normal = (baseVertices[0].position - topVertex.position).cross(baseVertices[3].position - topVertex.position).normalize())
                ),
                baseColor = Color.Red
            )
        )

        return Mesh(baseTriangles + wallTriangles)
    }

//    fun generatePyramidMesh(): Mesh {
//        val baseVertices = listOf(
//            Vector3(-2f, -2f, 0f), // Bottom-left
//            Vector3(2f, -2f, 0f),  // Bottom-right
//            Vector3(2f, 2f, 0f),   // Top-right
//            Vector3(-2f, 2f, 0f)   // Top-left
//        )
//
//        val topVertex = Vector3(0f, 0f, 4f) // Pyramid tip
//
//        // Assign unique colors for each wall
//        val wallColors = listOf(
//            Color.Red,
//            Color.Green,
//            Color.Blue,
//            Color.Yellow
//        )
//
//        // Create triangles for the pyramid walls
//        val triangles = mutableListOf<Triangle>()
//        for (i in baseVertices.indices) {
//            val nextIndex = (i + 1) % baseVertices.size
//            val wallTriangle = Triangle(
//                vertices = listOf(
//                    Vertex(baseVertices[i], normal = Vector3(0f, 0f, 1f), uv = Vector3(0f, 0f, 0f)),
//                    Vertex(baseVertices[nextIndex], normal = Vector3(0f, 0f, 1f), uv = Vector3(1f, 0f, 0f)),
//                    Vertex(topVertex, normal = Vector3(0f, 0f, 1f), uv = Vector3(0.5f, 1f, 0f))
//                ),
//                baseColor = wallColors[i] // Assign a unique color to the wall
//            )
//            triangles.add(wallTriangle)
//        }
//
//        // Create triangles for the pyramid base
//        val baseTriangle1 = Triangle(
//            vertices = listOf(
//                Vertex(baseVertices[0], normal = Vector3(0f, -1f, 0f), uv = Vector3(0f, 0f, 0f)),
//                Vertex(baseVertices[1], normal = Vector3(0f, -1f, 0f), uv = Vector3(1f, 0f, 0f)),
//                Vertex(baseVertices[2], normal = Vector3(0f, -1f, 0f), uv = Vector3(1f, 1f, 0f))
//            ),
//            baseColor = Color.Gray // Assign a neutral color for the base
//        )
//
//        val baseTriangle2 = Triangle(
//            vertices = listOf(
//                Vertex(baseVertices[0], normal = Vector3(0f, -1f, 0f), uv = Vector3(0f, 0f, 0f)),
//                Vertex(baseVertices[2], normal = Vector3(0f, -1f, 0f), uv = Vector3(1f, 1f, 0f)),
//                Vertex(baseVertices[3], normal = Vector3(0f, -1f, 0f), uv = Vector3(0f, 1f, 0f))
//            ),
//            baseColor = Color.Gray
//        )
//
//        triangles.addAll(listOf(baseTriangle1, baseTriangle2))
//
//        return Mesh(triangles)
//    }


    private val _pyramidMesh = MutableStateFlow(generatePyramidMesh()) // State for the pyramid mesh
    val pyramidMesh: StateFlow<Mesh> = _pyramidMesh

    private var rotationAngle = 0f // Current rotation angle around the X-axis

    init {
        startPyramidRotation()
    }

    // Start the coroutine to rotate the pyramid
    private fun startPyramidRotation() {
        viewModelScope.launch {
            while (true) {
                delay(16L) // Approximately 60 FPS (16 ms per frame)
                rotationAngle += 0.02f // Increment rotation angle (adjust speed here)
                _pyramidMesh.value = generateRotatedPyramidMesh(rotationAngle)
            }
        }
    }

    // Generate the rotated pyramid mesh
    private fun generateRotatedPyramidMesh(angle: Float): Mesh {
        val originalMesh = generatePyramidMesh()

        val rotatedTriangles = originalMesh.triangles.map { triangle ->
            val rotatedVertices = triangle.vertices.map { vertex ->
                val rotatedPosition = rotateX(vertex.position, angle)
                vertex.copy(position = rotatedPosition) // Update position while retaining normals and UVs
            }
            Triangle(rotatedVertices)
        }

        return Mesh(rotatedTriangles)
    }

    // Rotate a point around the X-axis
    private fun rotateX(point: Vector3, angle: Float): Vector3 {
        val cosAngle = kotlin.math.cos(angle)
        val sinAngle = kotlin.math.sin(angle)
        return Vector3(
            x = point.x,
            y = point.y * cosAngle - point.z * sinAngle,
            z = point.y * sinAngle + point.z * cosAngle
        )
    }
}

fun parseBezierSurface(filePath: String): List<Vector3> {
    // Read lines from the file
    val lines = File(filePath).readLines()

    // Parse each line into a Vertex
    return lines.map { line ->
        val components = line.split(" ").map { it.toFloat() } // Split line into x, y, z
        Vector3(components[0], components[1], components[2])  // Create a Vertex object
    }
}
