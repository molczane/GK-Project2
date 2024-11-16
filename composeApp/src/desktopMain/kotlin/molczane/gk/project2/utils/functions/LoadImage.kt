package molczane.gk.project2.utils.functions

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.File
import javax.imageio.ImageIO

fun loadImage(filePath: String): ImageBitmap {
    val bufferedImage = ImageIO.read(File(filePath))
    return bufferedImage.toComposeImageBitmap()
}