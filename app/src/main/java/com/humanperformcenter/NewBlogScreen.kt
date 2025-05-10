@file:OptIn(ExperimentalMaterial3Api::class)
package com.humanperformcenter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

import com.humanperformcenter.ui.components.AppCard

data class BlogEntry(val title: String, val date: String?)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NewBlogScreen(navController: NavHostController) {
    val blogEntries = listOf(
        BlogEntry("Founder's day", "12 de julio 2025"),
        BlogEntry("Exos 2025", "Julio 2025"),
        BlogEntry("Human del mes - Abril", "Abril 2025"),
        BlogEntry("Human Kids", "Abril 2025"),
        BlogEntry("Human del mes - Marzo", "Marzo 2025")
    )

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
        bottomBar = { NavigationBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(blogEntries) { entry ->
                AppCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            entry.date?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Ver más",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}