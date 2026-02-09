package com.humanperformcenter.ui.components.user

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EditableUserProfileImage(
    photoName: String?,
    photoUri: Uri?,
    onChangePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clickable { onChangePhotoClick() },
        contentAlignment = Alignment.BottomEnd
    ) {
        UserProfileImage(photoName = photoName, photoUri = photoUri)

        Box(
            modifier = Modifier
                .size(24.dp)
                .offset(y = (-12).dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.Gray, CircleShape)
                .clickable { onChangePhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Cambiar foto",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}