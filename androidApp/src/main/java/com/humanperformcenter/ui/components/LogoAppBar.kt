package com.humanperformcenter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.humanperformcenter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoAppBar(
    showBackArrow: Boolean,
    onBackNavClicked: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de la app",
                modifier = Modifier
                    .height(40.dp) // Ajusta al tamaño de tu logo
                    .padding(start = if (showBackArrow) 0.dp else 8.dp) // Evita colisión con la flecha
            )
        },
        navigationIcon = {
            if (showBackArrow) {
                IconButton(onClick = onBackNavClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFB71C1C),
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White
        )
    )
}
