package com.humanperformcenter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.data.model.product_service.ServiceItem
import com.humanperformcenter.ui.components.ConfirmCancelDialog
import com.humanperformcenter.ui.components.ProductCard
import com.humanperformcenter.ui.components.ProductOptionsDialog
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel

@Composable
fun MyProductsScreen(
    serviceProductViewModel: ServiceProductViewModel,
    navController: NavHostController,
    userViewModel: UserViewModel,
    daySessionViewModel: DaySessionViewModel,
    userId: Int
) {
    val processedProducts by serviceProductViewModel.productsState.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    // ----- NO TENGO NI IDEA DE LO QUE HACE ESTE TROZO DE CÓDIGO
    val userBookings by userViewModel.userBookings.collectAsStateWithLifecycle()

    LaunchedEffect(userBookings) {
        if (userBookings.isNotEmpty()) {
            daySessionViewModel.cargarFormularioSiProcede(userBookings)
        }
    }
    // ------------------------------------

    // El contenido puro de la UI
    MyProductsContent(
        productos = processedProducts,
        onProductClick = { product ->
            serviceProductViewModel.productoSeleccionado = product
            navController.navigate(ProductDetail(product.id))
        },
        onConfirmCancel = { targetId ->
            serviceProductViewModel.unassignProductFromUser(targetId, userId)
        }
    )
}

@Composable
fun MyProductsContent(
    productos: List<ServiceItem>,
    onProductClick: (ServiceItem) -> Unit,
    onConfirmCancel: (Int) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<ServiceItem?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (productos.isEmpty()) {
            Text(
                "No tienes productos contratados.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = productos, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = {
                            selectedProduct = product
                            showOptionsDialog = true
                        }
                    )
                }
            }
        }

        // Gestión de Diálogos
        if (showOptionsDialog && selectedProduct != null) {
            ProductOptionsDialog(
                productName = selectedProduct!!.name,
                onViewDetails = {
                    onProductClick(selectedProduct!!)
                    showOptionsDialog = false
                },
                onCancelRequest = { showCancelConfirmation = true },
                onDismiss = { showOptionsDialog = false }
            )
        }

        if (showCancelConfirmation && selectedProduct != null) {
            ConfirmCancelDialog(
                onConfirm = {
                    onConfirmCancel(selectedProduct!!.id)
                    showCancelConfirmation = false
                    showOptionsDialog = false
                    selectedProduct = null
                },
                onDismiss = { showCancelConfirmation = false }
            )
        }
    }
}