package com.travelguide.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelguide.AppContainer
import com.travelguide.auth.simpleFactory
import com.travelguide.ui.screens.profile.EditProfileScreen
import java.io.ByteArrayOutputStream

private fun Context.readBytes(uri: Uri): ByteArray {
    contentResolver.openInputStream(uri)?.use { input ->
        val bos = ByteArrayOutputStream()
        val buf = ByteArray(8 * 1024)
        while (true) {
            val r = input.read(buf)
            if (r <= 0) break
            bos.write(buf, 0, r)
        }
        return bos.toByteArray()
    }
    return ByteArray(0)
}

@Composable
fun EditProfileRoute(
    container: AppContainer,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val vm: ProfileViewModel = viewModel(
        factory = simpleFactory {
            ProfileViewModel(
                container.userRepository,
                container.authRepository,
                container.sessionManager
            )
        }
    )

    val state by vm.state.collectAsState()
    val editState by vm.editState.collectAsState()

    LaunchedEffect(Unit) {
        if (state.user == null) vm.loadMe()
    }

    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.readBytes(uri)
            if (bytes.isNotEmpty()) {
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                vm.uploadAvatar(bytes = bytes, mimeType = mime)
            }
        }
    }

    val user = state.user
    if (user != null) {
        EditProfileScreen(
            user = user,
            state = editState,
            onBackClick = onBackClick,
            onPickAvatarClick = { picker.launch("image/*") },
            onSaveClick = { username, phone, avatarUrl, homeCityId ->
                vm.saveProfile(
                    username = username,
                    phone = phone,
                    avatarUrl = avatarUrl,
                    homeCityId = homeCityId,
                    onSuccess = {
                        vm.loadMe()
                        onSaved()
                    }
                )
            }
        )
    }
}