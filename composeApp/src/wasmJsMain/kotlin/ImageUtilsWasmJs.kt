import com.momo.cardmaker.ImageUtils
import kotlinx.browser.window
import org.jetbrains.skia.*
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement

object ImageUtilsWasmJs : ImageUtils {
    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        // Create a canvas element
        val canvas = window.document.createElement("canvas") as HTMLCanvasElement
        val context = canvas.getContext("2d") as CanvasRenderingContext2D

        // Set canvas dimensions to match bitmap
        canvas.width = bitmap.width
        canvas.height = bitmap.height

        val uByteArray = bitmap.readPixels(
            dstInfo = ImageInfo(
                ColorInfo(
                    ColorType.RGBA_8888,
                    ColorAlphaType.OPAQUE,
                    ColorSpace.sRGB
                ), bitmap.width, bitmap.height
            )
        )?.asUByteArray()
        if (uByteArray != null) {
            // Create Skia Image from Bitmap
            val imageData = context.createImageData(bitmap.width.toDouble(), bitmap.height.toDouble())

            val pix = imageData.data.unsafeCast<Uint16Array>()

            for (i in uByteArray.indices) {
                pix[i] = uByteArray[i].toShort()
            }

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
