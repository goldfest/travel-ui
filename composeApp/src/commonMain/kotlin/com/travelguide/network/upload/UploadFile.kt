package com.travelguide.network.upload

data class UploadFile(
    val fileName: String,
    val bytes: ByteArray,
    val contentType: String = "image/jpeg"
)
