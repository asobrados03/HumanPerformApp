package com.humanperformcenter.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

class UserDocumentsCoordinator(
    private val context: Context
) {
    fun createTempCameraUri(): Uri {
        val file = File(
            context.cacheDir,
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun loadDocument(uri: Uri): SelectedDocument? {
        val documentBytes = uri.toByteArray() ?: return null
        val documentName = uri.resolveFileName()
        return SelectedDocument(name = documentName, bytes = documentBytes)
    }

    private fun Uri.resolveFileName(): String {
        val cursor = context.contentResolver.query(this, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                it.getString(index)
            } else {
                lastPathSegment.orEmpty()
            }
        } ?: lastPathSegment.orEmpty()
    }

    private fun Uri.toByteArray(): ByteArray? =
        context.contentResolver.openInputStream(this)?.use { inputStream ->
            inputStream.readBytes()
        }
}

data class SelectedDocument(
    val name: String,
    val bytes: ByteArray
)

@Composable
fun rememberUserDocumentsCoordinator(): UserDocumentsCoordinator {
    val context = LocalContext.current
    return remember(context) { UserDocumentsCoordinator(context) }
}
