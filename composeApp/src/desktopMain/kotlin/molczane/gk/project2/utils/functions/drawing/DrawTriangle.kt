package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import molczane.gk.project2.model.Triangle

fun DrawScope.drawTriangle(triangle: Triangle, color: Color, outlineOnly: Boolean = false) {
    val path = Path().apply {
        moveTo(triangle.vertices[0].position.x * 100 + size.width / 2, triangle.vertices[0].position.y * 100 + size.height / 2)
        lineTo(triangle.vertices[1].position.x * 100 + size.width / 2, triangle.vertices[1].position.y * 100 + size.height / 2)
        lineTo(triangle.vertices[2].position.x * 100 + size.width / 2, triangle.vertices[2].position.y * 100 + size.height / 2)
        close()
    }

    if (outlineOnly) {
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
    } else {
        drawPath(path, color, style = Fill)
    }
}

//fun DrawScope.drawTriangle(triangle: Triangle, color: Color) {
//    // Drawing each vertex as a point in 2D space
//    triangle.vertices.forEach { vertex ->
//        drawCircle(
//            color = color,
//            radius = 5.dp.toPx(),
//            center = Offset(
//                vertex.position.x * 100 + size.width / 2,
//                vertex.position.y * 100 + size.height / 2
//            )
//        )
//    }
//
//    // Convert 3D coordinates to 2D screen coordinates and draw the triangle
//    val path = Path().apply {
//        moveTo(
//            triangle.vertices[0].position.x * 100 + size.width / 2,
//            triangle.vertices[0].position.y * 100 + size.height / 2
//        )
//        lineTo(
//            triangle.vertices[1].position.x * 100 + size.width / 2,
//            triangle.vertices[1].position.y * 100 + size.height / 2
//        )
//        lineTo(
//            triangle.vertices[2].position.x * 100 + size.width / 2,
//            triangle.vertices[2].position.y * 100 + size.height / 2
//        )
//        close()
//    }
//    drawPath(path, color)
//}