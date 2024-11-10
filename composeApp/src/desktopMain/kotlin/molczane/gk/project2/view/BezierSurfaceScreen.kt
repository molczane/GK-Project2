package molczane.gk.project2.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.utils.functions.drawTriangle
import molczane.gk.project2.utils.functions.rotatePoint
import molczane.gk.project2.viewModel.BezierViewModel

@Composable
fun BezierSurfaceScreen(viewModel: BezierViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        val mesh by viewModel.mesh.collectAsState()

        // Canvas occupying 80% of the available width
        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
            // Sort triangles by average Z depth for 3D-like rendering
            val sortedTriangles = mesh.triangles.sortedByDescending { triangle ->
                triangle.vertices.map { it.position.z }.average()
            }

            // Draw each triangle
            sortedTriangles.forEach { triangle ->
                // Apply rotation to each vertex in the triangle
                val transformedVertices = triangle.vertices.map {
                    rotatePoint(it, viewModel.rotationAlpha, viewModel.rotationBeta)
                }

                // Create a new Triangle with transformed vertices
                val transformedTriangle = Triangle(transformedVertices)

                // Draw the transformed triangle with lighting applied
                drawTriangle(transformedTriangle, viewModel.calculateLighting(transformedTriangle))
            }
        }

        // Sliders occupying 20% of the available width
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Alpha rotation slider
            Text(text = "Alpha Rotation", fontSize = 14.sp)
            Slider(
                value = viewModel.rotationAlpha,
                onValueChange = { viewModel.updateRotation(it, viewModel.rotationBeta) },
                valueRange = -45f..45f
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Beta rotation slider
            Text(text = "Beta Rotation", fontSize = 14.sp)
            Slider(
                value = viewModel.rotationBeta,
                onValueChange = { viewModel.updateRotation(viewModel.rotationAlpha, it) },
                valueRange = -10f..10f
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Triangulation accuracy slider
            Text(text = "Triangulation Accuracy", fontSize = 14.sp)
            Slider(
                value = viewModel.triangulationAccuracy.toFloat(),
                onValueChange = { newAccuracy ->
                    viewModel.triangulationAccuracy = newAccuracy.toInt()
                    viewModel.updateMesh() // Regenerate the mesh with the new accuracy
                },
                valueRange = 1f..10f
            )
        }
    }
}


//@Composable
//fun BezierSurfaceScreen(viewModel: BezierViewModel) {
//    Row(modifier = Modifier.fillMaxSize()) {
//        val mesh by viewModel.mesh.collectAsState()
//
//        // Canvas occupying 80% of the available width
//        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
//            // Sort triangles by average Z depth
//            val sortedTriangles = mesh.triangles.sortedByDescending { triangle ->
//                triangle.vertices.map { it.position.z }.average()
//            }
//
//            // Draw each triangle
//            sortedTriangles.forEach { triangle ->
//                val transformedVertices = triangle.vertices.map {
//                    rotatePoint(it, viewModel.rotationAlpha, viewModel.rotationBeta)
//                }
//
//                // Create a new Triangle with transformed vertices
//                val transformedTriangle = Triangle(transformedVertices)
//
//                drawTriangle(transformedTriangle, viewModel.calculateLighting(transformedTriangle))
//            }
//        }
//
//        // Sliders occupying 20% of the available width
//        Column(
//            modifier = Modifier
//                .fillMaxHeight()
//                .weight(0.2f)
//                .padding(16.dp)
//                .background(Color.LightGray),
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Alpha rotation slider
//            Text(text = "Alpha Rotation", fontSize = 14.sp)
//            Slider(
//                value = viewModel.rotationAlpha,
//                onValueChange = { viewModel.updateRotation(it, viewModel.rotationBeta) },
//                valueRange = -45f..45f
//            )
//
//            // Spacer for padding between sliders
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Beta rotation slider
//            Text(text = "Beta Rotation", fontSize = 14.sp)
//            Slider(
//                value = viewModel.rotationBeta,
//                onValueChange = { viewModel.updateRotation(viewModel.rotationAlpha, it) },
//                valueRange = -10f..10f
//            )
//
//            // Spacer for padding between sliders
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Triangulation accuracy slider
//            Text(text = "Triangulation Accuracy", fontSize = 14.sp)
//            Slider(
//                value = viewModel.triangulationAccuracy.toFloat(),
//                onValueChange = { viewModel.triangulationAccuracy = it.toInt() },
//                valueRange = 1f..10f
//            )
//        }
//    }
//}