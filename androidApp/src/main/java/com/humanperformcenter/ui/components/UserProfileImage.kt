package com.humanperformcenter.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.network.ApiClient

@Composable
fun UserProfileImage(photoName: String? = null, photoUri: Uri? = null) {
    val baseUrl = "${ApiClient.baseUrl}/api/profile_pic/"
    when {
        // ① si hay URI local, la mostramos
        photoUri != null -> AsyncImage(
            model = photoUri,
            contentDescription = "Foto de usuario",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
        // ② si no URI pero sí nombre remoto, construimos la URL
        !photoName.isNullOrBlank() -> AsyncImage(
            model = "$baseUrl$photoName",
            contentDescription = "Foto de usuario",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
        // ③ fallback
        else -> Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar por defecto",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
