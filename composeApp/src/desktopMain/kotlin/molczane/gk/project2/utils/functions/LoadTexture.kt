package molczane.gk.project2.utils.functions

import androidx.compose.ui.graphics.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun loadTexture(filePath: String): Array<Array<Color>> {
    // Load the image using ImageIO
    val image: BufferedImage = ImageIO.read(File(filePath))

    // Extract the width and height of the image
    val width = image.width
    val height = image.height

    // Initialize a 2D array for storing colors
    val colorArray = Array(width) { Array(height) { Color.Black } }

    // Loop through the image pixels
    for (x in 0 until width) {
        for (y in 0 until height) {
            // Get the ARGB value of the pixel
            val argb = image.getRGB(x, y)

            // Extract the red, green, blue, and alpha components
            val alpha = (argb shr 24) and 0xFF
            val red = (argb shr 16) and 0xFF
            val green = (argb shr 8) and 0xFF
            val blue = argb and 0xFF

            // Convert to Compose Color and store in the array
            colorArray[x][y] = Color(
                red = red / 255f,
                green = green / 255f,
                blue = blue / 255f,
                alpha = alpha / 255f
            )
        }
    }

    return colorArray
}
