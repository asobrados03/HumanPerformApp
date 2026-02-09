package com.humanperformcenter.ui.components.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.humanperformcenter.ui.components.app.rememberShimmerBrush

@Composable
fun ServicesShimmer() {
    val shimmerBrush = rememberShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp), // Alineado con tu LazyColumn
        verticalArrangement = Arrangement.spacedBy(8.dp) // Mismo espacio que tu LazyColumn
    ) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(93.dp) // Altura estimada de tu AppCard
                    .background(shimmerBrush, RoundedCornerShape(20.dp))
            )
        }
    }
}