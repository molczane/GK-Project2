package molczane.gk.project2.utils.functions.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.model.Vertex
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


// basic version
fun DrawScope.fillPolygonWithScanLine(
    triangle: Triangle,
    scale: Float = 100f,
    calculateColorForPoint: (point: Vector3) -> Color,
    currentLightPos: Vector3
) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    val vertices = listOf(
        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
    )

    val sortedVertices = vertices.sortedBy { it.y }
    val yMin = sortedVertices.first().y.toInt()
    val yMax = sortedVertices.last().y.toInt()

    val edgeTable = mutableListOf<Edge>()
    for (i in sortedVertices.indices) {
        val v1 = sortedVertices[i]
        val v2 = sortedVertices[(i + 1) % sortedVertices.size]
        if (v1.y != v2.y) {
            val ymin = floor(min(v1.y, v2.y)).toInt()
            val ymax = ceil(max(v1.y, v2.y)).toInt()
            val xMin = if (v1.y < v2.y) v1.x else v2.x
            val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
            edgeTable.add(Edge(ymin, ymax, xMin, inverseSlope))
        }
    }

    val activeEdgeTable = mutableListOf<Edge>()

    val pixelBuffer = mutableListOf<Pair<Offset, Color>>()
    for (y in yMin until yMax) {
        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
        activeEdgeTable.removeAll { it.yMax <= y }
        activeEdgeTable.sortBy { it.xMin }

        for (i in activeEdgeTable.indices step 1) {
            if (i + 1 < activeEdgeTable.size) {
                val xStart = ceil(activeEdgeTable[i].xMin).toInt()
                val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()

                for (x in xStart..xEnd) {
                    val interpolatedPoint = interpolateBarycentric(
                        x.toFloat(),
                        y.toFloat(),
                        vertices,
                        triangle.vertices
                    )
                    val pointColor = calculateColorForPoint(interpolatedPoint)
                    drawRect(color = pointColor, topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = /*Size(1.dp.toPx(), 1.dp.toPx())*/ Size(1f, 1f)
                    )
                    //pixelBuffer.add(Offset(x.toFloat(), y.toFloat()) to pointColor)
                }
            }
        }

        for (edge in activeEdgeTable) {
            edge.xMin += edge.inverseSlope
        }
    }

//    pixelBuffer.forEach { (offset, color) ->
//        drawRect(color = color, topLeft = offset, size = Size(1f, 1f))
//    }

}

// Interpolate a point using barycentric coordinates
private fun interpolateBarycentric(
    x: Float,
    y: Float,
    vertices: List<Vector3>,
    triangleVertices: List<Vertex>
): Vector3 {
    val p = Vector3(x, y, 0f)
    val v0 = vertices[0]
    val v1 = vertices[1]
    val v2 = vertices[2]

    // Compute barycentric weights
    val denom = (v1.y - v2.y) * (v0.x - v2.x) + (v2.x - v1.x) * (v0.y - v2.y)
    val w0 = ((v1.y - v2.y) * (p.x - v2.x) + (v2.x - v1.x) * (p.y - v2.y)) / denom
    val w1 = ((v2.y - v0.y) * (p.x - v2.x) + (v0.x - v2.x) * (p.y - v2.y)) / denom
    val w2 = 1f - w0 - w1

    // Interpolate 3D position using barycentric weights
    val p0 = triangleVertices[0].position
    val p1 = triangleVertices[1].position
    val p2 = triangleVertices[2].position

    return (p0 * w0) + (p1 * w1) + (p2 * w2)
    //return (v0 * w0) + (v1 * w1) + (v2 * w2)
}

// Edge data structure
data class Edge(val yMin: Int, val yMax: Int, var xMin: Float, val inverseSlope: Float)

// coroutines used in this function
//fun DrawScope.fillPolygonWithScanLine(
//    triangle: Triangle,
//    scale: Float = 100f,
//    calculateColorForPoint: (point: Vector3) -> Color,
//    time: Float
//) {
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    val vertices = listOf(
//        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
//    )
//
//    val sortedVertices = vertices.sortedBy { it.y }
//    val yMin = sortedVertices.first().y.toInt()
//    val yMax = sortedVertices.last().y.toInt()
//
//    val edgeTable = mutableListOf<Edge>()
//    for (i in sortedVertices.indices) {
//        val v1 = sortedVertices[i]
//        val v2 = sortedVertices[(i + 1) % sortedVertices.size]
//        if (v1.y != v2.y) {
//            val ymin = floor(min(v1.y, v2.y)).toInt()
//            val ymax = ceil(max(v1.y, v2.y)).toInt()
//            val xMin = if (v1.y < v2.y) v1.x else v2.x
//            val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
//            edgeTable.add(Edge(ymin, ymax, xMin, inverseSlope))
//        }
//    }
//
//    val activeEdgeTable = mutableListOf<Edge>()
//
//    runBlocking {
//        // Lista korutyn dla równoległych zadań
//        val tasks = mutableListOf<Deferred<List<Pair<Offset, Color>>>>()
//
//        for (y in yMin until yMax) {
//            activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
//            activeEdgeTable.removeAll { it.yMax <= y }
//            activeEdgeTable.sortBy { it.xMin }
//
//            if (activeEdgeTable.size < 2) continue
//
//            val xStart = ceil(activeEdgeTable[0].xMin).toInt()
//            val xEnd = floor(activeEdgeTable[1].xMin).toInt()
//
//            // Przetwarzaj obliczenia wierszy równolegle
//            tasks.add(async(Dispatchers.Default) {
//                val pixels = mutableListOf<Pair<Offset, Color>>()
//
//                for (x in xStart..xEnd) {
//                    val interpolatedPoint = interpolateBarycentric(
//                        x.toFloat(),
//                        y.toFloat(),
//                        vertices,
//                        triangle.vertices
//                    )
//                    val pointColor = calculateColorForPoint(interpolatedPoint)
//                    pixels.add(Offset(x.toFloat(), y.toFloat()) to pointColor)
//                }
//
//                pixels
//            })
//
//            for (edge in activeEdgeTable) {
//                edge.xMin += edge.inverseSlope
//            }
//        }
//
//        // Czekaj na zakończenie wszystkich korutyn
//        val pixelBuffer = tasks.awaitAll().flatten()
//
//        // Rysuj wszystkie przetworzone piksele
//        pixelBuffer.forEach { (offset, color) ->
//            drawRect(color = color, topLeft = offset, size = Size(1f, 1f))
//        }
//    }
//}
