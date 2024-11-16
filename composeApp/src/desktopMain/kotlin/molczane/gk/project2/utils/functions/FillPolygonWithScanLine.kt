package molczane.gk.project2.utils.functions

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

import kotlinx.coroutines.*

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


// a bit optimized version
fun DrawScope.fillPolygonWithScanLine(
    triangle: Triangle,
    scale: Float = 100f,
    calculateColorForPoint: (point: Vector3) -> Color,
    time: Float
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

        for (i in activeEdgeTable.indices step 2) {
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
                    pixelBuffer.add(Offset(x.toFloat(), y.toFloat()) to pointColor)
                }
            }
        }

        for (edge in activeEdgeTable) {
            edge.xMin += edge.inverseSlope
        }
    }

    pixelBuffer.forEach { (offset, color) ->
        drawRect(color = color, topLeft = offset, size = Size(1f, 1f))
    }
}

// basic version
//fun DrawScope.fillPolygonWithScanLine(
//    triangle: Triangle,
//    scale: Float = 100f,
//    calculateColorForPoint: (point: Vector3) -> Color,
//    time: Float
//) {
//    // Transform triangle vertices to canvas coordinates
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    val vertices = listOf(
//        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
//    )
//
//    // Sort vertices by y-coordinate for scan-line filling
//    val sortedVertices = vertices.sortedBy { it.y }
//    val yMin = sortedVertices.first().y.toInt()
//    val yMax = sortedVertices.last().y.toInt()
//
//    // Build Edge Table
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
//    // Active Edge Table (AET)
//    val activeEdgeTable = mutableListOf<Edge>()
//
//    for (y in yMin until yMax) {
//        // Add edges to AET for the current scan-line
//        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
//        activeEdgeTable.removeAll { it.yMax <= y }
//        activeEdgeTable.sortBy { it.xMin }
//
//        for (i in activeEdgeTable.indices step 2) {
//            if (i + 1 < activeEdgeTable.size) {
//                val xStart = ceil(activeEdgeTable[i].xMin).toInt()
//                val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()
//
//                for (x in xStart..xEnd) {
//                    // Calculate barycentric coordinates
//                    val interpolatedPoint = interpolateBarycentric(
//                        x = x.toFloat(),
//                        y = y.toFloat(),
//                        vertices = sortedVertices,
//                        triangleVertices = triangle.vertices
//                    )
//
//                    // Calculate color for the interpolated point
//                    val pointColor = calculateColorForPoint(interpolatedPoint)
//
//                    drawRect(
//                        color = pointColor,
//                        topLeft = Offset(x.toFloat(), y.toFloat()),
//                        size = androidx.compose.ui.geometry.Size(1f, 1f)
//                    )
//                }
//            }
//        }
//
//        // Update x-coordinates in AET for the next scan-line
//        for (edge in activeEdgeTable) {
//            edge.xMin += edge.inverseSlope
//        }
//    }
//}

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
}

// Edge function for barycentric coordinates
private fun edgeFunction(v0: Vector3, v1: Vector3, p: Vector3): Float {
    return (p.x - v0.x) * (v1.y - v0.y) - (p.y - v0.y) * (v1.x - v0.x)
}

// Edge data structure
data class Edge(val yMin: Int, val yMax: Int, var xMin: Float, val inverseSlope: Float)

// version with the same color for whole triangles
//fun DrawScope.fillPolygonWithScanLine(triangle: Triangle,
//                                      color: Color,
//                                      scale: Float = 100f,
//                                      calulateColorForPoint: (
//                                          point: Vector3
//                                      ) -> Color
//) {
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    val vertices = listOf(
//        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
//        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
//    ).sortedBy { it.y }
//
//    val yMin = vertices.first().y.toInt()
//    val yMax = ceil(vertices.last().y).toInt()
//
//    val edgeTable = mutableListOf<Edge>()
//    for (i in vertices.indices) {
//        val v1 = vertices[i]
//        val v2 = vertices[(i + 1) % vertices.size]
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
//    for (y in yMin until yMax) {
//        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
//        activeEdgeTable.removeAll { it.yMax <= y }
//        activeEdgeTable.sortBy { it.xMin }
//
//        for (i in activeEdgeTable.indices step 2) {
//            if (i + 1 < activeEdgeTable.size) {
//                val xStart = ceil(activeEdgeTable[i].xMin).toInt()
//                val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()
//                drawRect(
//                    color = color,
//                    topLeft = Offset(xStart.toFloat(), y.toFloat()),
//                    size = androidx.compose.ui.geometry.Size((xEnd - xStart).toFloat(), 1f)
//                )
//            }
//        }
//
//        for (edge in activeEdgeTable) {
//            edge.xMin += edge.inverseSlope
//        }
//    }
//}
//
//// Edge data structure
//data class Edge(val yMin: Int, val yMax: Int, var xMin: Float, val inverseSlope: Float)
