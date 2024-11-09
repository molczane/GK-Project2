package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import java.io.File

// Updated function to parse the Bezier surface file
fun parseBezierSurface(filePath: String): List<Vertex> {
    val controlPoints = mutableListOf<Vertex>()
    File(filePath).useLines { lines ->
        lines.forEach { line ->
            val (x, y, z) = line.split(" ").map { it.toFloat() }
            controlPoints.add(Vertex(Vector3(x, y, z))) // Create Vertex with Vector3 position
        }
    }
    return controlPoints
}