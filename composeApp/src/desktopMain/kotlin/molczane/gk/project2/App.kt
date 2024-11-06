package molczane.gk.project2

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import molczane.gk.project2.view.BezierSurfaceScreen
import molczane.gk.project2.viewModel.BezierViewModel

@Composable
@Preview
fun App() {
    val viewModel = BezierViewModel()
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            BezierSurfaceScreen(viewModel)
        }
    }
}