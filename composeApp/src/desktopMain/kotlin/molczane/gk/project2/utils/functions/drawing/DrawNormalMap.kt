package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Vector3

fun DrawScope.drawNormalMap(normals: Array<Array<Vector3>>, scale: Float = 1f) {
    val height = normals.size
    val width = normals[0].size

    for (y in normals.indices) {
        for (x in normals[y].indices) {
            val normal = normals[y][x]

            // Map the normal vector back to RGB
            val r = ((normal.x + 1) / 2).coerceIn(0f, 1f)
            val g = ((normal.y + 1) / 2).coerceIn(0f, 1f)
            val b = normal.z.coerceIn(0f, 1f)

            val color = Color(r, g, b)

            // Draw a pixel at (x, y)
            drawRect(
                color = color,
                topLeft = Offset(x * scale, y * scale),
                size = Size(scale, scale)
            )
        }
    }
}
