package com.humanperformcenter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.humanperformcenter.app.navigation.MenuOption
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.NavigationBar
import com.humanperformcenter.ui.components.UserProfileImage
import com.humanperformcenter.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    onEditProfile: () -> Unit,
    onViewProfile: () -> Unit,
    onMenuClick: (MenuOption) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    val newProfileUriLiveData = remember(backStackEntry) {
        backStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("new_profile_uri")
            ?: MutableLiveData<String?>(null)
    }

    val newUriString by newProfileUriLiveData.observeAsState(initial = null)

    val photoUri = remember(newUriString) {
        newUriString?.toUri()
    }

    val user by userViewModel.userData.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
    }

    LaunchedEffect(user) {
        user?.let { currentUser ->
            userViewModel.loadBalance(currentUser.id)
        }
    }

    val balance by userViewModel.balance.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = false,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        bottomBar = { NavigationBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            user == null -> {
                FullScreenLoading()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFB71C1C))
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (photoUri != null){
                                    UserProfileImage(user!!.profilePictureName, photoUri)
                                } else {
                                    UserProfileImage(user!!.profilePictureName)
                                }
                                //Boton de texto con el texto "Editar"
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = user!!.fullName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user!!.email,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = user!!.phone,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Card(
                                    colors = cardColors(containerColor = Color.Yellow),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Text(
                                        text = "Saldo: $balance €",
                                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    FilledTonalButton(onClick = onViewProfile) {
                                        Text("Mi perfil")
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    OutlinedButton(
                                        onClick = onEditProfile,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White
                                        ),
                                        border = BorderStroke(1.dp, Color.DarkGray)
                                    ) {
                                        Text("Editar perfil")
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }

                    val items = listOf(
                        MenuOption.CONFIGURACION to "Configuración",
                        MenuOption.FAVORITOS      to "Mis favoritos",
                        MenuOption.DOCUMENTO      to "Documento",
                        MenuOption.VER_PAGO           to "Ver metodos de pago",
                        MenuOption.MONEDERO_VIRTUAL to "Monedero Virtual",
                        MenuOption.ANADIR_CUPON   to "Añadir cupón"
                    )

                    items(items) { (option, title) ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
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
                                    contentDescription = "Ir"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
