package molczane.gk.project2.model

import kotlin.math.sqrt

// Define Vector3 data class
data class Vector3(val x: Float, val y: Float, val z: Float) {
    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z
    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun normalize(): Vector3 {
        val len = length()
        return Vector3(x / len, y / len, z / len)
    }

    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)
}

