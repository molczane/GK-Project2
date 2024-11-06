package molczane.gk.project2.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.viewModel.BezierViewModel

@Composable
fun BezierSurfaceScreen(viewModel: BezierViewModel) {
    Column {
        // Alpha rotation slider
        Text(text = "Alpha Rotation")
        Slider(
            value = viewModel.rotationAlpha,
            onValueChange = { viewModel.updateRotation(it, viewModel.rotationBeta) },
            valueRange = -45f..45f
        )

        // Beta rotation slider
        Text(text = "Beta Rotation")
        Slider(
            value = viewModel.rotationBeta,
            onValueChange = { viewModel.updateRotation(viewModel.rotationAlpha, it) },
            valueRange = -10f..10f
        )

        // Triangulation accuracy slider
        Text(text = "Triangulation Accuracy")
        Slider(
            value = viewModel.triangulationAccuracy.toFloat(),
            onValueChange = { viewModel.triangulationAccuracy = it.toInt() },
            valueRange = 1f..10f
        )

        // Canvas to display the mesh
        val meshState by viewModel.mesh.collectAsState() // collect the mesh state outside the Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            meshState.triangles.forEach { triangle ->
                val color = viewModel.calculateLighting(triangle)
                drawTriangle(triangle, color)
            }
        }
    }
}

fun DrawScope.drawTriangle(triangle: Triangle, color: Color) {
    // Drawing each vertex as a point for now. In a real application, transform vertices to 2D space.
    triangle.vertices.forEach { vertex ->
        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = Offset(vertex.x * 100 + size.width / 2, vertex.y * 100 + size.height / 2)
        )
    }
}
