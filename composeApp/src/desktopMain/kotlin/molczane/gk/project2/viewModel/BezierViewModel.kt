package molczane.gk.project2.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import molczane.gk.project2.model.Mesh
import molczane.gk.project2.model.Triangle
import molczane.gk.project2.model.Vertex

class BezierViewModel : ViewModel() {
    private val _mesh = MutableStateFlow(generateSampleMesh())
    val mesh: StateFlow<Mesh> get() = _mesh

    var rotationAlpha by mutableStateOf(0f)
    var rotationBeta by mutableStateOf(0f)
    var triangulationAccuracy by mutableStateOf(3) // Placeholder for triangulation accuracy slider

    // Sample function to create a basic mesh for testing purposes
    private fun generateSampleMesh(): Mesh {
        // Placeholder - generate a simple set of triangles for now
        val vertices = listOf(
            Vertex(-0.5f, -0.5f, 0f),
            Vertex(0.5f, -0.5f, 0f),
            Vertex(0f, 0.5f, 0f)
        )
        val triangles = listOf(Triangle(vertices))
        return Mesh(triangles)
    }

    // Method to update rotation based on user input
    fun updateRotation(alpha: Float, beta: Float) {
        rotationAlpha = alpha
        rotationBeta = beta
        updateMesh() // Recompute mesh based on new rotation values
    }

    // Placeholder for updating the mesh
    private fun updateMesh() {
        // Implement rotation transformation and other calculations here
    }

    // Placeholder for color calculation (based on lighting model)
    fun calculateLighting(triangle: Triangle): Color {
        // Simple color for demonstration, replace with real lighting calculation
        return Color(0xFFAAAAAA)
    }
}
