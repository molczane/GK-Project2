package molczane.gk.project2.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.utils.functions.drawTriangle
import molczane.gk.project2.utils.functions.drawTriangleOutline
import molczane.gk.project2.utils.functions.drawTrianglePixelByPixel
import molczane.gk.project2.utils.functions.fillPolygonWithScanLine
import molczane.gk.project2.utils.functions.rotatePoint
import molczane.gk.project2.viewModel.BezierViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BezierSurfaceScreen(viewModel: BezierViewModel) {
    var isMeshMode by remember { mutableStateOf(true) } // Toggle between mesh and filled mode

    // Collect all StateFlow values
    val rotationAlpha by viewModel.rotationAlpha.collectAsState()
    val rotationBeta by viewModel.rotationBeta.collectAsState()
    val triangulationAccuracy by viewModel.triangulationAccuracy.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        val mesh by viewModel.mesh.collectAsState()

        val coroutineScope = rememberCoroutineScope()
        var transformedTriangles by remember { mutableStateOf(emptyList<Pair<Triangle, Color>>()) }

        // diffrent version
//        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
//
//            coroutineScope.launch(Dispatchers.Default) {
//                // Perform calculations on a background thread
//                val sortedTriangles = mesh.triangles.sortedByDescending { triangle ->
//                    triangle.vertices.map { it.position.z }.average()
//                }
//
//                val results = sortedTriangles.map { triangle ->
//                    async {
//                        val transformedVertices = triangle.vertices.map {
//                            rotatePoint(it, rotationAlpha, rotationBeta)
//                        }
//                        val transformedTriangle = Triangle(transformedVertices)
//                        val color =
//                            viewModel.calculateLighting(transformedTriangle, currentTime)
//                        transformedTriangle to color
//                    }
//                }.awaitAll()
//
//                // Update the state with the transformed triangles and colors on the main thread
//                transformedTriangles = results
//            }
//
//            transformedTriangles.forEach { (transformedTriangle, color) ->
//                fillPolygonWithScanLine(
//                    triangle = transformedTriangle,
//                    color = if (isMeshMode) Color.Transparent else color
//                )
//                drawTriangleOutline(transformedTriangle, color)
//            }
//
//        }

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
                    rotatePoint(it, rotationAlpha, rotationBeta)
                }

                // Create a new Triangle with transformed vertices
                val transformedTriangle = Triangle(transformedVertices)

//                // Call the pixel-by-pixel draw function, passing the drawContext
//                drawTrianglePixelByPixel(
//                    triangle = transformedTriangle,
//                    color = if (isMeshMode) Color.Black else viewModel.calculateLighting(transformedTriangle, currentTime),
//                )

                fillPolygonWithScanLine(
                    triangle = transformedTriangle,
                    color = if (isMeshMode) Color.Transparent else viewModel.calculateLighting(transformedTriangle, currentTime),
                )

                drawTriangleOutline(transformedTriangle, viewModel.calculateLighting(transformedTriangle, currentTime))

//                // Draw the triangle in either mesh or filled mode
//                if (isMeshMode) {
//                    drawTriangle(transformedTriangle, Color.Black, outlineOnly = true) // Mesh outline
//                } else {
//                    drawTriangle(transformedTriangle, viewModel.calculateLighting(transformedTriangle, currentTime), outlineOnly = false) // Filled
//                }
            }
        }

        // Updated control panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .background(Color.LightGray),
            verticalArrangement = Arrangement.Center
        ) {
            // Alpha rotation slider
            Text(
                text = "Alpha Rotation (Z-axis)", fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            Slider(
                value = rotationAlpha,
                onValueChange = { viewModel.updateRotation(it, rotationBeta) },
                valueRange = -2*0.7853982f..2*0.7853982f,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Beta rotation slider
            Text(
                text = "Beta Rotation (X-axis)", fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            Slider(
                value = rotationBeta,
                onValueChange = { viewModel.updateRotation(rotationAlpha, it) },
                valueRange = 0f..4*0.17453292f,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Triangulation accuracy slider
            Text(
                text = "Triangulation Accuracy", fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            Slider(
                value = triangulationAccuracy.toFloat(),
                onValueChange = { viewModel.updateTriangulation(it.toInt()) },
                valueRange = 1f..80f,
                modifier = Modifier.padding(8.dp)
            )

            // ... rest of the UI remains the same ...

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle between mesh and filled triangles
            Text(
                text = "Display Mode", fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp),
            ) {
                Text(text = "Filled")
                Switch(
                    checked = isMeshMode,
                    onCheckedChange = { isMeshMode = it }
                )
                Text(text = "Mesh")
            }
        }
    }
}