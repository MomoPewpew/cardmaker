import com.momo.cardmaker.ImageUtils
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

expect object ImageUtilsDesktop : ImageUtils {
    override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String)
}

actual object ImageUtilsDesktop : ImageUtils {
    actual override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        val bufferedImage = BufferedImage(bitmap.width, bitmap.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.graphics
        graphics.drawImage(bitmap.toBufferedImage(), 0, 0, null)
        graphics.dispose()

        // Get the default download directory
        val defaultDir = System.getProperty("user.home")

        // Create a JFileChooser instance
        val fileChooser = JFileChooser(defaultDir)
        fileChooser.dialogTitle = "Save Image"

        // Create a FileFilter for PNG files
        val pngFilter = FileNameExtensionFilter("PNG Image", "png")
        fileChooser.setFileFilter(pngFilter)

        // Set a default filename suggestion
        val defaultFileName = "card.png"
        fileChooser.setSelectedFile(File(defaultDir, defaultFileName))

        // Show the save dialog
        val returnVal = fileChooser.showSaveDialog(Frame())

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            var selectedFile = fileChooser.selectedFile

            // Ensure filename ends with .png
            val filename = selectedFile.absolutePath
            val extension = if (filename.lastIndexOf('.') > 0) {
                filename.substring(filename.lastIndexOf('.'))
            } else {
                ""
            }

            if (extension.lowercase() != ".png") {
                val newFilename = "$filename.png"
                selectedFile = File(newFilename)
            }

            // Check if file already exists
            if (selectedFile.exists()) {
                val confirmation = JOptionPane.showConfirmDialog(
                    Frame(),
                    "The file '${selectedFile.name}' already exists. Overwrite?",
                    "Save Image Confirmation",
                    JOptionPane.YES_NO_OPTION
                )

                if (confirmation != JOptionPane.YES_OPTION) {
                    return
                }
            }

            // Write the image to the selected file
            ImageIO.write(bufferedImage, "png", selectedFile)
        }
    }
}