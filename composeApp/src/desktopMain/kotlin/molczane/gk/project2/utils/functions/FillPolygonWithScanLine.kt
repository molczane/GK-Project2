package molczane.gk.project2.utils.functions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

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
