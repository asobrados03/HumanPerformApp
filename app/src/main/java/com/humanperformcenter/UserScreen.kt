package com.humanperformcenter

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.ui.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    navController: NavHostController,
    user: com.humaneperformcenter.shared.data.model.User,
    onEditProfile: () -> Unit,
    onMenuClick: (MenuOption) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 8.dp)
                        )
                    }
                },
                colors = topAppBarColors(
                    containerColor = Color(0xFFB71C1C), // Rojo fuerte, ajustable
                    titleContentColor = Color.White
                ),
                navigationIcon = {},
                actions = {}
            )
        },
        bottomBar = { NavigationBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // -- HEADER ROJO --
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB71C1C))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // foto de perfil
                    UserProfileImage(user.profilePictureUrl)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = user.phone,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.height(12.dp))

                    // saldo
                    Card(
                        colors = cardColors(containerColor = Color.Yellow),
                        shape = RoundedCornerShape(8.dp),
                        elevation = cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Saldo: € ${"%.2f".format(user.balance)}",
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    FilledTonalButton(onClick = {
                        navController.navigate(Screen.EditProfileScreen.route)
                    }) {
                        Text("Editar perfil")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // -- MENÚ --
            val items = listOf(
                MenuOption.FAVORITOS to "Mis favoritos",
                MenuOption.CHAT        to "Chat",
                MenuOption.DOCUMENTO   to "Documento",
                MenuOption.PAGO        to "Método de pago",
                MenuOption.VER_PAGO    to "Ver método de pago"
            )

            Column {
                items.forEach { (option, title) ->
                    AppCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = { onMenuClick(option) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = title, fontSize = 16.sp)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class MenuOption {
    FAVORITOS, CHAT, DOCUMENTO, PAGO, VER_PAGO
}
// Función reutilizable para mostrar la imagen de perfil del usuario
@Composable
fun UserProfileImage(photoUrl: String?) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto de usuario",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    } else {
        Image(
            painter = painterResource(R.drawable.avatar_default),
            contentDescription = "Avatar por defecto",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}