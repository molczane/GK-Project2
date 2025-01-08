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
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vector3
import molczane.gk.project2.utils.functions.drawing.drawTriangleOutline
import molczane.gk.project2.utils.functions.drawing.fillPolygonWithScanLine
import molczane.gk.project2.utils.functions.rotatePoint
import molczane.gk.project2.viewModel.BezierViewModel
import java.awt.FileDialog
import java.awt.Frame
import javax.swing.JColorChooser

@Composable
fun BezierSurfaceScreen(viewModel: BezierViewModel) {
    var isMeshMode by remember { mutableStateOf(true) } // Toggle between mesh and filled mode

    val isNormalMappingTurnedOn by viewModel.isNormalMappingTurnedOn.collectAsState()
    val isTextureTurnedOn by viewModel.isTextureTurnedOn.collectAsState()

    // Collect all StateFlow values
    val rotationAlpha by viewModel.rotationAlpha.collectAsState()
    val isLightAnimationRunning by viewModel.isLightAnimationRunning.collectAsState()
    val rotationBeta by viewModel.rotationBeta.collectAsState()
    val triangulationAccuracy by viewModel.triangulationAccuracy.collectAsState()
    val currentLightPos = viewModel.lightPos.collectAsState()
    val k_d = viewModel.k_d.collectAsState()
    val k_s = viewModel.k_s.collectAsState()
    val m = viewModel.m.collectAsState()

    val mesh by viewModel.mesh.collectAsState()

    val isRedReflectorTurnedOn = viewModel.isRedLightTurnedOn.collectAsState()
    val isGreenReflectorTurnedOn = viewModel.isGreenLightTurnedOn.collectAsState()
    val isBlueReflectorTurnedOn = viewModel.isBlueLightTurnedOn.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        val pyramidMesh = viewModel.generatePyramidMesh()

        Canvas(modifier = Modifier.fillMaxHeight().weight(0.8f)) {
            val canvasWidth = size.width.toInt()
            val canvasHeight = size.height.toInt()

            // Initialize the Z-buffer
            val zBuffer = Array(canvasHeight) { FloatArray(canvasWidth) { Float.POSITIVE_INFINITY } }

            val combinedTriangles = pyramidMesh.triangles + mesh.triangles

            val sortedTriangles = combinedTriangles.sortedByDescending { triangle ->
                triangle.vertices.map { it.position.z }.average()
            }

            val transformedTriangles = sortedTriangles.map { triangle ->
                val transformedVertices = triangle.vertices.map {
                    rotatePoint(it, rotationAlpha, rotationBeta)
                }
                Triangle(transformedVertices)
            }

            // Draw the transformed normals if needed
            //            drawTriangleNormals(
            //                triangles = transformedTriangles,
            //                scale = 100f,
            //                normalLength = 50f,
            //                normalColor = Color.Red
            //            )

            // Render the transformed mesh
            transformedTriangles.forEach { transformedTriangle ->
                if (isMeshMode) {
                    drawTriangleOutline(transformedTriangle, Color.Black)
                } else {
                    if (isTextureTurnedOn) {
                        fillPolygonWithScanLine(
                            triangle = transformedTriangle,
                            calculateColorForPoint = { point ->
                                val calculateLightingForPoint =
                                    viewModel.calculateLightingForPointWithTexture(
                                        point = point,
                                        triangle = transformedTriangle
                                    )
                                calculateLightingForPoint
                            },
                            currentLightPos = currentLightPos.value
                        )
                    }
                    else {
                        fillPolygonWithScanLine(
                            triangle = transformedTriangle,
                            scale = 100f,
                            calculateColorForPoint = { point ->
                                val calculateLightingForPoint = viewModel.calculateLightingForPoint(
                                    point = point,
                                    triangle = transformedTriangle
                                )
                                calculateLightingForPoint
                            },
                            currentLightPos = currentLightPos.value,
                            zBuffer = zBuffer // Pass Z-buffer
                        )
                    }
                }
            }

            //drawNormalsFromMap(mesh, viewModel.normalMap)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.2f)
                .background(Color.LightGray)
                .padding(4.dp), // Kompaktowe odstępy od krawędzi
            verticalArrangement = Arrangement.spacedBy(4.dp) // Odstępy między elementami
        ) {
            // Alpha rotation slider
            Text("Alpha Rotation (Z-axis)", fontSize = 12.sp)
            Slider(
                value = rotationAlpha,
                onValueChange = { viewModel.updateRotation(it, rotationBeta) },
                valueRange = -2 * 0.7853982f..2 * 0.7853982f,
                modifier = Modifier.height(32.dp) // Kompaktowa wysokość
            )

            // Beta rotation slider
            Text("Beta Rotation (X-axis)", fontSize = 12.sp)
            Slider(
                value = rotationBeta,
                onValueChange = { viewModel.updateRotation(rotationAlpha, it) },
                valueRange = 0f..8 * 0.17453292f,
                modifier = Modifier.height(32.dp)
            )

            // Triangulation accuracy slider
            Text("Triangulation Accuracy", fontSize = 12.sp)
            Slider(
                value = triangulationAccuracy.toFloat(),
                onValueChange = { viewModel.updateTriangulation(it.toInt()) },
                valueRange = 1f..128f,
                modifier = Modifier.height(32.dp)
            )

            // k_d slider
            Text("k_d coefficient", fontSize = 12.sp)
            Slider(
                value = k_d.value,
                onValueChange = { viewModel.updateK_d(it) },
                valueRange = 0f..1f,
                modifier = Modifier.height(32.dp)
            )

            // k_s slider
            Text("k_s coefficient", fontSize = 12.sp)
            Slider(
                value = k_s.value,
                onValueChange = { viewModel.updateK_s(it) },
                valueRange = 0f..1f,
                modifier = Modifier.height(32.dp)
            )

            // m slider
            Text("m coefficient", fontSize = 12.sp)
            Slider(
                value = m.value,
                onValueChange = { viewModel.updateM(it) },
                valueRange = 1f..100f,
                modifier = Modifier.height(32.dp)
            )

            // Light animation toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Light Animation", fontSize = 12.sp)
                Switch(
                    checked = isLightAnimationRunning,
                    onCheckedChange = { viewModel.toggleLightAnimation() }
                )
            }

            // Display mode toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Display Mode: ", fontSize = 12.sp)
                Text("Filled", fontSize = 12.sp)
                Switch(
                    checked = isMeshMode,
                    onCheckedChange = { isMeshMode = it },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text("Mesh", fontSize = 12.sp)
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        viewModel.updateNormalMapping(true, openFileDialog("src/normal-maps"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Choose Normal Map", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = {
                        val color = openColorPicker()
                        if (color != null) {
                            viewModel.updateColor(color.toVector3())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Choose Color", fontSize = 10.sp)
                }
            }

            Button(
                onClick = {
                    val textureFile = openFileDialog("src/textures")
                    if (textureFile != null) {
                        viewModel.updateTexture(true, textureFile)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Choose Texture", fontSize = 10.sp)
            }

            Button(
                onClick = {
                    viewModel.updateTexture(false, null)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Turn off texture", fontSize = 10.sp)
            }

            Button(
                onClick = {
                    viewModel.updateNormalMapping(false, null)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Turn off normal mapping", fontSize = 10.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Dostosuj rozmieszczenie
            ) {
                Button(
                    onClick = {
                        if (isRedReflectorTurnedOn.value) {
                            viewModel.updateRedReflector(false)
                        } else {
                            viewModel.updateRedReflector(true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Red Reflector", fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        if (isGreenReflectorTurnedOn.value) {
                            viewModel.updateGreenReflector(false)
                        } else {
                            viewModel.updateGreenReflector(true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Green Reflector", fontSize = 10.sp)
                }
            }
            Button(
                onClick = {
                    if (isBlueReflectorTurnedOn.value) {
                        viewModel.updateBlueReflector(false)
                    } else {
                        viewModel.updateBlueReflector(true)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Blue Reflector", fontSize = 10.sp)
            }
        }
    }
}

fun openFileDialog(defaultDirectory: String = System.getProperty("user.home")): String? {
    val fileDialog = FileDialog(null as Frame?, "Select a File", FileDialog.LOAD)
    fileDialog.directory = defaultDirectory // Set the default directory
    fileDialog.isVisible = true
    return fileDialog.file?.let { fileDialog.directory + it }
}

fun openColorPicker(): java.awt.Color? {
    return JColorChooser.showDialog(null, "Choose a Color", java.awt.Color.WHITE)
}

fun java.awt.Color.toVector3(): Vector3 {
    return Vector3(
        red / 255f,
        green / 255f,
        blue / 255f)
}
