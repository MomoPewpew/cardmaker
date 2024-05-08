package com.momo.cardmaker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap

class ViewModel {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    suspend fun saveImage(bitmap: Bitmap, filePath: String) {
        imageUtils?.saveBitmapToFile(bitmap, filePath)
    }

    fun triggerSaveImage(bitmap: Bitmap, filePath: String) {
        viewModelScope.launch {
            saveImage(bitmap, filePath)
        }
    }
}