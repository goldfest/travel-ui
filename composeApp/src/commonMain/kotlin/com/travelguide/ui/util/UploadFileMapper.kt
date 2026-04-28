package com.travelguide.ui.util

import android.content.Context
import android.net.Uri
import com.travelguide.network.upload.UploadFile

fun Uri.toUploadFile(context: Context): UploadFile? {
    val resolver = context.contentResolver
    val type = resolver.getType(this) ?: "image/jpeg"
    val extension = when (type) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        else -> "jpg"
    }

    val bytes = resolver.openInputStream(this)?.use { it.readBytes() } ?: return null
    val safeName = "travel_photo_${System.currentTimeMillis()}.$extension"

    return UploadFile(
        fileName = safeName,
        bytes = bytes,
        contentType = type
    )
}
