package molczane.gk.project2.utils.functions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

//fun DrawScope.fillPolygonWithScanLine(triangle: Triangle, color: Color, scale: Float = 100f) {
//    // Scale the vertices to fit the canvas coordinate system
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    val v1 = Vector3(
//        x = triangle.vertices[0].position.x * scale + canvasCenterX,
//        y = triangle.vertices[0].position.y * scale + canvasCenterY,
//        z = triangle.vertices[0].position.z
//    )
//    val v2 = Vector3(
//        x = triangle.vertices[1].position.x * scale + canvasCenterX,
//        y = triangle.vertices[1].position.y * scale + canvasCenterY,
//        z = triangle.vertices[1].position.z
//    )
//    val v3 = Vector3(
//        x = triangle.vertices[2].position.x * scale + canvasCenterX,
//        y = triangle.vertices[2].position.y * scale + canvasCenterY,
//        z = triangle.vertices[2].position.z
//    )
//
//    val vertices = listOf(v1, v2, v3).sortedBy { it.y }
//
//    val sortedVertices = vertices.sortedBy { it.y }
//
//    val yMin = sortedVertices.first().y.toInt()
//    val yMax = ceil(sortedVertices.last().y).toInt()
//
//    val edgeTable = mutableListOf<Edge>()
//    for (i in 0 until sortedVertices.size - 1) {
//        val (v1, v2) = sortedVertices[i] to sortedVertices[i + 1]
//        if (v1.y != v2.y) {
//            val yMin = minOf(v1.y, v2.y).toInt()
//            val yMax = ceil(maxOf(v1.y, v2.y)).toInt()
//            val xMin = if (v1.y < v2.y) v1.x else v2.x
//            val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
//            edgeTable.add(Edge(yMin, yMax, xMin, inverseSlope))
//        }
//    }
//
//    val activeEdgeTable = mutableListOf<Edge>()
//    for (y in yMin until yMax) {
//        // Add new edges from the edge table to the active edge table
//        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
//        // Remove edges from the active edge table that are no longer relevant
//        activeEdgeTable.removeAll { it.yMax <= y }
//        // Sort the active edge table by x-coordinate
//        activeEdgeTable.sortBy { it.xMin }
//
//        // Fill pixels between each pair of intersections
//        for (i in activeEdgeTable.indices step 2) {
//            if (i + 1 < activeEdgeTable.size) {
//                val xStart = ceil(activeEdgeTable[i].xMin).toInt()
//                val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()
//                for (x in xStart..xEnd) {
//                    drawRect(color = color, topLeft = Offset(x.toFloat(), y.toFloat()), size = Size(5f, 5f))
//                }
//            }
//        }
//
//        // Update the x-coordinate of each active edge for the next scan line
//        for (edge in activeEdgeTable) {
//            edge.xMin += edge.inverseSlope
//        }
//    }
//
////    val yMin = vertices.first().y.toInt()
////    val yMax = ceil(vertices.last().y).toInt()
////
////    val edgeTable = mutableListOf<Edge>()
////    for (i in 0 until vertices.size - 1) {
////        val (v1, v2) = vertices[i] to vertices[i + 1]
////        if (v1.y != v2.y) {
////            val ymin = minOf(v1.y, v2.y).toInt()
////            val ymax = ceil(maxOf(v1.y, v2.y)).toInt()
////            val xMin = if (v1.y < v2.y) v1.x else v2.x
////            val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
////            edgeTable.add(Edge(ymin, ymax, xMin, inverseSlope))
////        }
////    }
////
////    val aet = mutableListOf<Edge>()
////    for (y in yMin until yMax) {
////        aet.addAll(edgeTable.filter { it.yMin == y })
////        aet.removeAll { it.yMax <= y }
////        aet.sortBy { it.xMin }
////
////        for (i in aet.indices step 2) {
////            if (i + 1 < aet.size) {
////                val xStart = ceil(aet[i].xMin).toInt()
////                val xEnd = floor(aet[i + 1].xMin).toInt()
////                for (x in xStart..xEnd) {
////                    drawRect(color = color, topLeft = Offset(x.toFloat(), y.toFloat()), size = Size(2f, 2f))
////                }
////            }
////        }
////
////        for (edge in aet) {
////            edge.xMin += edge.inverseSlope
////        }
////    }
//}

//fun DrawScope.fillPolygonWithScanLine(triangle: Triangle, color: Color, scale: Float = 100f) {
//    // Scale the vertices to fit the canvas coordinate system
//    val canvasCenterX = size.width / 2
//    val canvasCenterY = size.height / 2
//
//    // Transform vertices to canvas space with scaling
//    val v1 = Vector3(
//        x = triangle.vertices[0].position.x * scale + canvasCenterX,
//        y = triangle.vertices[0].position.y * scale + canvasCenterY,
//        z = triangle.vertices[0].position.z
//    )
//    val v2 = Vector3(
//        x = triangle.vertices[1].position.x * scale + canvasCenterX,
//        y = triangle.vertices[1].position.y * scale + canvasCenterY,
//        z = triangle.vertices[1].position.z
//    )
//    val v3 = Vector3(
//        x = triangle.vertices[2].position.x * scale + canvasCenterX,
//        y = triangle.vertices[2].position.y * scale + canvasCenterY,
//        z = triangle.vertices[2].position.z
//    )
//
//    // Sort vertices by their y-coordinate
//    val vertices = listOf(v1, v2, v3).sortedBy { it.y }
//    val yMin = vertices.first().y.toInt()
//    val yMax = ceil(vertices.last().y).toInt()
//
//    // Build edge table with each edge's ymin, ymax, xMin, and inverse slope
//    val edgeTable = mutableListOf<Edge>()
//    for (i in vertices.indices) {
//        val v1 = vertices[i]
//        val v2 = vertices[(i + 1) % vertices.size]
//
//        if (v1.y == v2.y) continue // Skip horizontal edges
//
//        val ymin = minOf(v1.y, v2.y).toInt()
//        val ymax = ceil(maxOf(v1.y, v2.y)).toInt()
//        val xMin = if (v1.y < v2.y) v1.x else v2.x
//        val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
//        edgeTable.add(Edge(ymin, ymax, xMin, inverseSlope))
//    }
//
//    // Initialize Active Edge Table (AET)
//    val activeEdgeTable = mutableListOf<Edge>()
//
//    // Process each scan-line from yMin to yMax
//    for (y in yMin until yMax) {
//        // Add edges to AET for the current scan-line
//        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
//
//        // Remove edges from AET where the maximum y of the edge has been reached
//        activeEdgeTable.removeAll { it.yMax <= y }
//
//        // Sort AET by x-coordinate
//        activeEdgeTable.sortBy { it.xMin }
//
//        // Ensure AET has an even number of edges for proper filling
//        if (activeEdgeTable.size % 2 != 0) continue
//
//        // Draw pixel spans between pairs of intersections in AET
//        for (i in activeEdgeTable.indices step 2) {
//            val xStart = ceil(activeEdgeTable[i].xMin).toInt()
//            val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()
//            for (x in xStart..xEnd) {
//                drawRect(color = color, topLeft = Offset(x.toFloat(), y.toFloat()), size = Size(1f, 1f))
//            }
//        }
//
//        // Update x-coordinates in AET for the next scan-line
//        for (edge in activeEdgeTable) {
//            edge.xMin += edge.inverseSlope
//        }
//    }
//}

fun DrawScope.fillPolygonWithScanLine(triangle: Triangle, color: Color, scale: Float = 100f) {
    val canvasCenterX = size.width / 2
    val canvasCenterY = size.height / 2

    val vertices = listOf(
        triangle.vertices[0].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[1].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f),
        triangle.vertices[2].position * scale + Vector3(canvasCenterX, canvasCenterY, 0f)
    ).sortedBy { it.y }

    val yMin = vertices.first().y.toInt()
    val yMax = ceil(vertices.last().y).toInt()

    val edgeTable = mutableListOf<Edge>()
    for (i in vertices.indices) {
        val v1 = vertices[i]
        val v2 = vertices[(i + 1) % vertices.size]
        if (v1.y != v2.y) {
            val ymin = floor(min(v1.y, v2.y)).toInt()
            val ymax = ceil(max(v1.y, v2.y)).toInt()
            val xMin = if (v1.y < v2.y) v1.x else v2.x
            val inverseSlope = (v2.x - v1.x) / (v2.y - v1.y)
            edgeTable.add(Edge(ymin, ymax, xMin, inverseSlope))
        }
    }

    val activeEdgeTable = mutableListOf<Edge>()
    for (y in yMin until yMax) {
        activeEdgeTable.addAll(edgeTable.filter { it.yMin == y })
        activeEdgeTable.removeAll { it.yMax <= y }
        activeEdgeTable.sortBy { it.xMin }

        for (i in activeEdgeTable.indices step 2) {
            if (i + 1 < activeEdgeTable.size) {
                val xStart = ceil(activeEdgeTable[i].xMin).toInt()
                val xEnd = floor(activeEdgeTable[i + 1].xMin).toInt()
                drawRect(
                    color = color,
                    topLeft = Offset(xStart.toFloat(), y.toFloat()),
                    size = androidx.compose.ui.geometry.Size((xEnd - xStart).toFloat(), 1f)
                )
            }
        }

        for (edge in activeEdgeTable) {
            edge.xMin += edge.inverseSlope
        }
    }
}


// Edge data structure
data class Edge(val yMin: Int, val yMax: Int, var xMin: Float, val inverseSlope: Float)
