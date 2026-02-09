package com.humanperformcenter.ui.components.user

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.humanperformcenter.shared.data.network.ApiClient

@Composable
fun UserProfileImage(
    photoName: String? = null,
    photoUri: Uri? = null,
    size: Dp = 80.dp,
    onError: ((Throwable?) -> Unit)? = null,
) {
    val context = LocalContext.current

    // Determina la fuente de la imagen: URI local o URL remota construida de forma segura.
    val data: Any? = remember(photoUri, photoName) {
        when {
            photoUri != null -> photoUri
            !photoName.isNullOrBlank() ->  "${ApiClient.baseUrl}/profile_pic/$photoName"
            else -> null
        }
    }

    // Configura la petición de Coil con crossfade, cache y listeners de error/éxito.
    val request = remember(data) {
        data?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                // Transformación circular para suavizar las esquinas si no se clipea.
                .transformations(RoundedCornersTransformation(999f))
                .listener(
                    onError = { _, result -> onError?.invoke(result.throwable) },
                    onSuccess = { _, _ -> onError?.invoke(null) },
                )
                .build()
        }
    }

    SubcomposeAsyncImage(
        model = request,
        contentDescription = "Foto de usuario",
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size / 3),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        error = {
            // Fallback visible si falla la red o la URL
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar por defecto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape),
            )
        },
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            // El borde se aplica una vez que se determina el tamaño y la forma
            .border(2.dp, Color.White, CircleShape),
    )
}
