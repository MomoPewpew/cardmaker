package com.momo.cardmaker

import org.jetbrains.skia.Bitmap

/** This interface is implemented by the Platform-specific code that is responsible for converting Bitmaps into image files. */
interface ImageUtils {
    /** Convert the provided Bitmap into an image file and prompt the client to download it.
     * @param bitmap A skia Bitmap that holds the rendered image.
     * @param filePath The default filename, in the case that prompting the user for a name is not the norm on this platform.
     * */
    suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String)
}