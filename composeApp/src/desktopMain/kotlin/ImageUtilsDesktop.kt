import androidx.compose.ui.graphics.asSkiaBitmap
import com.momo.cardmaker.ImageUtils
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

expect object ImageUtilsDesktop : ImageUtils {
    override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String)
}

actual object ImageUtilsDesktop : ImageUtils {
    actual override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        val bufferedImage = BufferedImage(bitmap.width, bitmap.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = bufferedImage.graphics
        graphics.drawImage(bitmap.toBufferedImage(), 0, 0, null)
        graphics.dispose()

        val file = File(filePath)
        val outputStream = FileOutputStream(file)
        ImageIO.write(bufferedImage, "png", outputStream)
        outputStream.close()
    }
}