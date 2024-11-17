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
import androidx.compose.ui.window.Dialog
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.utils.functions.drawNormals
import molczane.gk.project2.utils.functions.drawTriangleNormals
import molczane.gk.project2.utils.functions.drawTriangleOutline
import molczane.gk.project2.utils.functions.fillPolygonWithScanLine
import molczane.gk.project2.utils.functions.loadImage
import molczane.gk.project2.utils.functions.rotatePoint
import molczane.gk.project2.viewModel.BezierViewModel
import java.awt.FileDialog
import java.awt.Frame

@Composable
fun BezierSurfaceScreen(viewModel: BezierViewModel) {
    var isMeshMode by remember { mutableStateOf(true) } // Toggle between mesh and filled mode

    var showDialog by remember { mutableStateOf(true) }
    var selectedFile by remember { mutableStateOf<String?>(null) }


    // Collect all StateFlow values
    val rotationAlpha by viewModel.rotationAlpha.collectAsState()
    val isLightAnimationRunning by viewModel.isLightAnimationRunning.collectAsState()
    val rotationBeta by viewModel.rotationBeta.collectAsState()
    val triangulationAccuracy by viewModel.triangulationAccuracy.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val texture = loadImage("src/images/texture-1.png")
    val currentLightPos = viewModel.lightPos.collectAsState()
    val k_d = viewModel.k_d.collectAsState()
    val k_s = viewModel.k_s.collectAsState()
    val m = viewModel.m.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        val mesh by viewModel.mesh.collectAsState()

        val coroutineScope = rememberCoroutineScope()
        //var transformedTriangles by remember { mutableStateOf(emptyList<Pair<Triangle, Color>>()) }

        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
            val sortedTriangles = mesh.triangles.sortedByDescending { triangle ->
                triangle.vertices.map { it.position.z }.average()
            }

            val transformedTriangles = sortedTriangles.map { triangle ->
                val transformedVertices = triangle.vertices.map {
                    rotatePoint(it, rotationAlpha, rotationBeta)
                }
                Triangle(transformedVertices)
            }

            // Draw the transformed normals
            drawTriangleNormals(
                triangles = transformedTriangles,
                scale = 100f,
                normalLength = 100f,
                normalColor = Color.Red
            )

            // Render the transformed mesh
            transformedTriangles.forEach { transformedTriangle ->
                if (isMeshMode) {
                    drawTriangleOutline(transformedTriangle, Color.Black)
                } else {
                    fillPolygonWithScanLine(
                        triangle = transformedTriangle,
                        calculateColorForPoint = { point ->
                            viewModel.calculateLightingForPoint(
                                point = point,
                                triangle = transformedTriangle
                            )
                        },
                        currentLightPos = currentLightPos.value
                    )
                }
            }
        }


        // Canvas occupying 80% of the available width
//        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
//            // Sort triangles by average Z depth for 3D-like rendering
//            val sortedTriangles = mesh.triangles.sortedByDescending { triangle ->
//                triangle.vertices.map { it.position.z }.average()
//            }
//
//            // Draw each triangle
//            sortedTriangles.forEach { triangle ->
//                // Apply rotation to each vertex in the triangle
//                val transformedVertices = triangle.vertices.map {
//                    rotatePoint(it, rotationAlpha, rotationBeta)
//                }
//
//                // Create a new Triangle with transformed vertices
//                val transformedTriangle = Triangle(transformedVertices)
//
//                if(isMeshMode) {
//                    fillPolygonWithScanLine(
//                        triangle = transformedTriangle,
//                        calculateColorForPoint = { point ->
//                            viewModel.calculateLightingForPoint(
//                                point = point,
//                                triangle = transformedTriangle
//                            )
//                        },
//                        currentLightPos = currentLightPos.value
////                        texture = texture,
////                        calculateUVForPoint = { x, y, vertices, triangleVertices ->
////                            viewModel.interpolateUV(x, y, vertices, triangleVertices)
////                        }
//                    )
//                }
//                else {
//                    drawTriangleOutline(transformedTriangle, Color.Black)
//                }
//            }
//        }

        // Updated control panel
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .background(Color.LightGray),
            verticalArrangement = Arrangement.Center,
        ) {
            // Alpha rotation slider
            Text(
                text = "Alpha Rotation (Z-axis)", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = rotationAlpha,
                onValueChange = { viewModel.updateRotation(it, rotationBeta) },
                valueRange = -2*0.7853982f..2*0.7853982f,
                modifier = Modifier.padding(4.dp)
            )

            // Beta rotation slider
            Text(
                text = "Beta Rotation (X-axis)", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = rotationBeta,
                onValueChange = { viewModel.updateRotation(rotationAlpha, it) },
                valueRange = 0f..8*0.17453292f,
                modifier = Modifier.padding(4.dp)
            )

            // Triangulation accuracy slider
            Text(
                text = "Triangulation Accuracy", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = triangulationAccuracy.toFloat(),
                onValueChange = { viewModel.updateTriangulation(it.toInt()) },
                valueRange = 1f..80f,
                modifier = Modifier.padding(4.dp)
            )

            Text(
                text = "k_d coefficient", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = k_d.value,
                onValueChange = { viewModel.updateK_d(it) },
                valueRange = 0f..1f,
                modifier = Modifier.padding(4.dp)
            )

            Text(
                text = "k_s coefficient", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = k_s.value,
                onValueChange = { viewModel.updateK_s(it) },
                valueRange = 0f..1f,
                modifier = Modifier.padding(4.dp)
            )

            Text(
                text = "m coefficient", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Slider(
                value = k_d.value,
                onValueChange = { viewModel.updateM(it) },
                valueRange = 1f..100f,
                modifier = Modifier.padding(4.dp)
            )

            // Toggle light animation
            Text(
                text = "Light Animation", fontSize = 14.sp,
                modifier = Modifier.padding(4.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp),
            ) {
                Text(text = if (isLightAnimationRunning) "On" else "Off")
                Switch(
                    checked = isLightAnimationRunning,
                    onCheckedChange = { viewModel.toggleLightAnimation() }
                )
            }

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

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Choose a file", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        selectedFile = openFileDialog()
                        showDialog = false
                    }) {
                        Text("Open File Dialog")
                    }
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

fun openFileDialog(): String? {
    val fileDialog = FileDialog(null as Frame?, "Select a File", FileDialog.LOAD)
    fileDialog.isVisible = true
    return fileDialog.file?.let { fileDialog.directory + it }
}