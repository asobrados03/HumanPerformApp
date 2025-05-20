package com.humanperformcenter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.shared.data.model.Professional
import com.humanperformcenter.shared.data.model.ProfessionalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<Professional> = mockProfessionals,
    onSelect: (Professional) -> Unit = {},
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val grouped = favorites.groupBy { it.type }

        Spacer(Modifier.width(20.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            grouped.forEach { (type, list) ->
                item {
                    Text(
                        text = when (type) {
                            ProfessionalType.TRAINER      -> "Entrenadores"
                            ProfessionalType.PHYSIO       -> "Fisioterapeutas"
                            ProfessionalType.NUTRITIONIST -> "Nutricionistas"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(list) { prof ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(prof) },
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    )  {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if (!prof.photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(prof.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = prof.name,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(text = prof.name, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

}

val mockProfessionals = listOf(
    Professional(id = "1",  name = "Juan Sanz",    type = ProfessionalType.TRAINER),
    Professional(id = "2",  name = "Pablo Sanz",   type = ProfessionalType.TRAINER),
    Professional(id = "3",  name = "Idaira Prieto", type = ProfessionalType.PHYSIO),
    Professional(id = "4",  name = "Guillermo Duque", type = ProfessionalType.TRAINER),
    Professional(id = "5",  name = "Jorge Mínguez", type = ProfessionalType.TRAINER),
    Professional(id = "6",  name = "Daniel Barroso", type = ProfessionalType.TRAINER),
    Professional(id = "7",  name = "Sergio Sanz",   type = ProfessionalType.TRAINER),
    Professional(id = "8",  name = "Javier Seco",   type = ProfessionalType.PHYSIO),
    Professional(id = "10", name = "María Jimeno",  type = ProfessionalType.PHYSIO),
    Professional(id = "11", name = "Isabel Álvaro", type = ProfessionalType.PHYSIO),
    Professional(id = "12", name = "Adrián Pinilla", type = ProfessionalType.TRAINER),
    Professional(id = "13", name = "Raúl Orejudo",  type = ProfessionalType.TRAINER)
)