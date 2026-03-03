package com.travelguide.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.travelguide.profile.readPickedImage
import kotlinx.coroutines.launch

@Composable
fun AvatarPicker(
    enabled: Boolean = true,
    onUpload: suspend (bytes: ByteArray, mimeType: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Стабильный контракт: есть почти во всех версиях AndroidX
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        uploading = true
        error = null

        scope.launch {
            try {
                val picked = readPickedImage(context, uri)
                onUpload(picked.bytes, picked.mimeType)
            } catch (t: Throwable) {
                error = t.message ?: "Upload failed"
            } finally {
                uploading = false
            }
        }
    }

    Button(
        enabled = enabled && !uploading,
        onClick = { launcher.launch("image/*") }
    ) {
        Text(if (uploading) "Загрузка..." else "Загрузить аватар (файл)")
    }

    if (error != null) {
        Text(text = "Ошибка: $error", color = MaterialTheme.colorScheme.error)
    }
}