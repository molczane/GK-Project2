package molczane.gk.project2.utils.functions

import molczane.gk.project2.model.Vector3
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

// Function to read an image and generate a map of normal vectors
fun loadNormalMap(imagePath: String): Array<Array<Vector3>> {
    val file = File(imagePath)
    if (!file.exists() || !file.isFile) throw IllegalArgumentException("Invalid image path: $imagePath")

    val image: BufferedImage = ImageIO.read(file)

    val width = image.width
    val height = image.height

    // Create a 2D array to hold the normal vectors
    val normalMap = Array(height) { Array(width) { Vector3(0f, 0f, 0f) } }

    for (y in 0 until height) {
        for (x in 0 until width) {
            val argb = image.getRGB(x, y)
            val r = (argb shr 16 and 0xFF) / 255f // Red channel
            val g = (argb shr 8 and 0xFF) / 255f  // Green channel
            val b = (argb and 0xFF) / 255f       // Blue channel

            // Convert RGB to normal vector, assuming (r, g, b) maps to (x, y, z) in [-1, 1]
            val normal = Vector3(
                x = 2f * r - 1f,
                y = 2f * g - 1f,
                z = 2f * b - 1f
            ).normalize()

            normalMap[y][x] = normal
        }
    }

    return normalMap
}
