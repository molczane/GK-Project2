package molczane.gk.project2.utils.functions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3

fun DrawScope.drawTriangleNormals(
    triangles: List<Triangle>,
    scale: Float = 100f,
    normalLength: Float = 50f, // Adjust length of the normal vectors
    normalColor: Color = Color.Red // Color for the normal vectors
) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    triangles.forEach { triangle ->
        // Scale and transform the vertices
        val scaledVertices = triangle.vertices.map { vertex ->
            vertex.position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
        }

        // Calculate the barycentric center (geometric center of the triangle)
        val barycentricCenter = (
                triangle.vertices[0].position +
                        triangle.vertices[1].position +
                        triangle.vertices[2].position
                ) / 3f * scale + Vector3(canvasCenterX, canvasCenterY, 0f)

        // Interpolate the normal at the center using barycentric coordinates
        val centerNormal = (
                triangle.vertices[0].normal +
                        triangle.vertices[1].normal +
                        triangle.vertices[2].normal
                ) / 3f

        // Normalize the interpolated normal
        val normalizedNormal = centerNormal.normalize()

        // Calculate the start and end points of the normal vector
        val start = barycentricCenter
        val end = barycentricCenter + normalizedNormal * normalLength

        // Draw the normal vector as a line
        drawLine(
            color = normalColor,
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            strokeWidth = 2f // Adjust thickness of the normal vector lines
        )
    }
}
