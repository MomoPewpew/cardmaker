import com.momo.cardmaker.ImageUtils
import org.jetbrains.skia.Bitmap
import kotlinx.browser.window
import org.khronos.webgl.Uint8ClampedArray
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement

object ImageUtilsWasmJs : ImageUtils {
    override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        // Create a canvas element
        val canvas = window.document.createElement("canvas") as HTMLCanvasElement
        val context = canvas.getContext("2d") as CanvasRenderingContext2D

        // Set canvas dimensions to match bitmap
        canvas.width = bitmap.width
        canvas.height = bitmap.height

        val bitArray = bitmap.readPixels()
        if (bitArray != null) {
            // Create Skia Image from Bitmap
            val clampedArray: Uint8ClampedArray = Uint8ClampedArray(bitArray.size)

            for (i in 1..<bitArray.size) {
                clampedArray.set(i, bitArray.get(i))
            }

            val imageData = context.createImageData(bitmap.width.toDouble(), bitmap.height.toDouble())
            imageData.data.set(clampedArray)

            // Draw Skia Image onto canvas
            context.putImageData(imageData, 0.0, 0.0)

            // Convert canvas content to PNG data URL
            val dataUrl = canvas.toDataURL("image/png")

            // Create a link element and set its properties
            val downloadLink = window.document.createElement("a") as HTMLAnchorElement
            downloadLink.href = dataUrl
            downloadLink.download = filePath

            // Simulate a click on the link to trigger the download
            downloadLink.click()
        }
    }
}
