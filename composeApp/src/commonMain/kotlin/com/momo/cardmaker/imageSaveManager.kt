package com.momo.cardmaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap

/** Manages saving images to the device storage. */
class ImageSaveManager {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    /**
     * Call the platform-specific file conversion and save prompt. Do not run this synchronously. Call triggerSaveImage instead.
     * @param bitmap A skia Bitmap that holds the rendered image.
     * @param filePath The default filename, in the case that prompting the user for a name is not the norm on this platform.
     */
    private suspend fun saveImage(bitmap: Bitmap, filePath: String) {
        imageUtils?.saveBitmapToFile(bitmap, filePath)
    }

    /**
     * Asynchronously trigger the platform-specific file conversion and save prompt.
     * @param bitmap A skia Bitmap that holds the rendered image.
     * @param filePath The default filename, in the case that prompting the user for a name is not the norm on this platform.
     */
    fun triggerSaveImage(bitmap: Bitmap, filePath: String) {
        viewModelScope.launch {
            saveImage(bitmap, filePath)
        }
    }
}