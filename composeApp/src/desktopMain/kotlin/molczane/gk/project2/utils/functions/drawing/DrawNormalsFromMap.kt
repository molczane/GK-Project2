package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Vector3

fun DrawScope.drawNormalsFromMap(
    mesh: Mesh,
    normalMap: Array<Array<Vector3>>, // Precomputed normals
    scale: Float = 100f,               // Scale factor for the surface
    arrowLength: Float = 100f,          // Length of normal vectors
    color: Color = Color.Red          // Color for the normals
) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    // Ensure the normal map matches the mesh structure
    val rows = normalMap.size
    val cols = if (rows > 0) normalMap[0].size else 0

    // Iterate over the vertices in the mesh and draw normals
    mesh.triangles.forEach { triangle ->
        triangle.vertices.forEachIndexed { index, vertex ->
            val normal = normalMap[index / cols][index % cols].normalize()

            // Transform and scale the vertex position
            val startX = vertex.position.x * scale + canvasCenterX
            val startY = vertex.position.y * scale + canvasCenterY

            // Calculate the start and end points for the arrow
            val start = Offset(startX, startY)
            val end = Offset(
                start.x + normal.x * arrowLength,
                start.y - normal.y * arrowLength // Invert Y for screen coordinates
            )

            // Draw the normal vector as a line
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 2f
            )
        }
    }
}
