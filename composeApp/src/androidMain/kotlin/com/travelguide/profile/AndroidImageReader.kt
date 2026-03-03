package com.travelguide.profile

import android.content.Context
import android.net.Uri

data class PickedImage(
    val bytes: ByteArray,
    val mimeType: String
)

fun readPickedImage(context: Context, uri: Uri): PickedImage {
    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException("Cannot open InputStream for uri=$uri")
    return PickedImage(bytes = bytes, mimeType = mime)
}