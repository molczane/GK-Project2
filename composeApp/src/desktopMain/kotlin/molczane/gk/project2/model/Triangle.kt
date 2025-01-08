package molczane.gk.project2.model

import androidx.compose.ui.graphics.Color

data class Triangle(
    val vertices: List<Vertex>,
    val baseColor: Color = Color.White // Default color for non-pyramid triangles
)
