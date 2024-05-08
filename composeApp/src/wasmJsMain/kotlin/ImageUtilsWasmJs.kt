import com.momo.cardmaker.ImageUtils
import kotlinx.browser.document
import org.jetbrains.skia.Bitmap
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

object ImageUtilsWasmJs : ImageUtils {
    override suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String) {
        val byteArray = bitmap.readPixels()

        if (byteArray != null) {
            val jsArray = JsArray<JsAny?>()

            val blob = Blob(blobParts = jsArray, options = BlobPropertyBag(type = "image/png"))
            val url = URL.createObjectURL(blob)

            val link = document.createElement("card") as HTMLAnchorElement
            link.href = url
            link.download = "card.png"

            link.click()

            URL.revokeObjectURL(url)
        }
    }
}