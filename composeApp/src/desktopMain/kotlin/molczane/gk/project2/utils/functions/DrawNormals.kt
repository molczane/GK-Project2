package molczane.gk.project2.utils.functions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3

fun DrawScope.drawNormals(
    triangles: List<Triangle>,
    scale: Float = 100f,
    normalLength: Float = 50f, // Adjust the length of the normal vectors
    normalColor: Color = Color.Red // Color for the normal vectors
) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    // Iterate over all vertices in the mesh
    triangles.forEach { triangle ->
        triangle.vertices.forEach { vertex ->
            val start = vertex.position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
            val end = start + vertex.normal * normalLength

            drawLine(
                color = normalColor,
                start = Offset(start.x, start.y),
                end = Offset(end.x, end.y),
                strokeWidth = 2f // Adjust thickness of the normal vector lines
            )
        }
    }
}


//fun DrawScope.drawNormals(
//    mesh: Mesh,
//    scale: Float = 100f,
//    normalLength: Float = 50f, // Adjust the length of the normal vectors
//    normalColor: Color = Color.Red // Color for the normal vectors
//) {
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    // Iterate over all vertices in the mesh
//    mesh.triangles.forEach { triangle ->
//        triangle.vertices.forEach { vertex ->
//            val start = vertex.position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
//            val end = start + vertex.normal * normalLength
//
//            drawLine(
//                color = normalColor,
//                start = Offset(start.x, start.y),
//                end = Offset(end.x, end.y),
//                strokeWidth = 2f // Adjust thickness of the normal vector lines
//            )
//        }
//    }
//}
