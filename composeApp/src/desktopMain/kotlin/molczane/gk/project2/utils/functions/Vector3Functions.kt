package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3

// Extension functions for vector operations
operator fun Vector3.times(other: Vector3) = Vector3(x * other.x, y * other.y, z * other.z)
fun Vector3.cross(other: Vector3) = Vector3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x
)