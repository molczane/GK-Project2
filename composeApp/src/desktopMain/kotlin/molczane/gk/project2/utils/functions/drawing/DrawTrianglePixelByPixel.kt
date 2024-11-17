package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun DrawScope.drawTrianglePixelByPixel(
    triangle: Triangle,
    color: Color,
    scale: Float = 100f // Scale to map triangle coordinates to canvas size
) {
    // Scale the vertices to fit the canvas coordinate system
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    val v1 = Vector3(
        x = triangle.vertices[0].position.x * scale + canvasCenterX,
        y = triangle.vertices[0].position.y * scale + canvasCenterY,
        z = triangle.vertices[0].position.z
    )
    val v2 = Vector3(
        x = triangle.vertices[1].position.x * scale + canvasCenterX,
        y = triangle.vertices[1].position.y * scale + canvasCenterY,
        z = triangle.vertices[1].position.z
    )
    val v3 = Vector3(
        x = triangle.vertices[2].position.x * scale + canvasCenterX,
        y = triangle.vertices[2].position.y * scale + canvasCenterY,
        z = triangle.vertices[2].position.z
    )

    // Define the bounding box for the triangle within the canvas bounds
    val minX = max(0, floor(min(v1.x, min(v2.x, v3.x))).toInt())
    val maxX = min(size.width.toInt(), ceil(max(v1.x, max(v2.x, v3.x))).toInt())
    val minY = max(0, floor(min(v1.y, min(v2.y, v3.y))).toInt())
    val maxY = min(size.height.toInt(), ceil(max(v1.y, max(v2.y, v3.y))).toInt())

    // Accumulate points within the triangle
    val points = mutableListOf<Offset>()
    for (x in minX..maxX) {
        for (y in minY..maxY) {
            val p = Vector3(x.toFloat(), y.toFloat(), 0f)
            if (isPointInTriangleBarycentric(p, v1, v2, v3)) {
                points.add(Offset(x.toFloat(), y.toFloat()))
            }
        }
    }

    // Draw accumulated points
    drawPoints(
        points = points,
        pointMode = PointMode.Points,
        color = color,
        strokeWidth = 1f
    )
}

// Function to check if a point is inside a triangle using Barycentric coordinates
fun isPointInTriangleBarycentric(p: Vector3, v1: Vector3, v2: Vector3, v3: Vector3): Boolean {
    val denominator = ((v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y))
    val a = ((v2.y - v3.y) * (p.x - v3.x) + (v3.x - v2.x) * (p.y - v3.y)) / denominator
    val b = ((v3.y - v1.y) * (p.x - v3.x) + (v1.x - v3.x) * (p.y - v3.y)) / denominator
    val c = 1 - a - b

    // Check if point is within the triangle
    return a >= 0 && b >= 0 && c >= 0
}
