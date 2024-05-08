package com.momo.cardmaker

import org.jetbrains.skia.Bitmap

interface ImageUtils {
    suspend fun saveBitmapToFile(bitmap: Bitmap, filePath: String)
}