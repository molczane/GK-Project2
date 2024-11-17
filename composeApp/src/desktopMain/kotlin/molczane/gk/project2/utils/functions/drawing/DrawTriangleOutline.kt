package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3

fun DrawScope.drawTriangleOutline(triangle: Triangle, color: Color, scale: Float = 100f) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    val vertices = listOf(
        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
    )

    // Draw each edge of the triangle
    for (i in vertices.indices) {
        val start = Offset(vertices[i].x, vertices[i].y)
        val end = Offset(vertices[(i + 1) % vertices.size].x, vertices[(i + 1) % vertices.size].y)
        drawLine(color = color, start = start, end = end, strokeWidth = 1f)
    }
}
