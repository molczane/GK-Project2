package molczane.gk.project2

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Frame

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(width = 1200.dp, height = 900.dp), // Set fixed size here
        title = "GK-Project2 - Ernest Mołczan",
    ) {
        App()
    }
}