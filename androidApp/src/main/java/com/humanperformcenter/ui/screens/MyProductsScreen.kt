package com.humanperformcenter.ui.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.ActiveProductDetail
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.presentation.ui.ActionUiState
import com.humanperformcenter.shared.presentation.ui.RefundUiState
import com.humanperformcenter.shared.presentation.ui.UnassignEvent
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.ui.components.product.ConfirmCancelDialog
import com.humanperformcenter.ui.components.app.ErrorComponent
import com.humanperformcenter.ui.components.product.MyProductsShimmer
import com.humanperformcenter.ui.components.product.ProductCard
import com.humanperformcenter.ui.components.product.ProductOptionsDialog
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel

@Composable
fun MyProductsScreen(
    serviceProductViewModel: ServiceProductViewModel,
    stripeViewModel: StripeViewModel,
    navController: NavHostController,
    userId: Int
) {
    // Recolectamos el estado completo de la Sealed Class
    val productsUiState by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        serviceProductViewModel.loadUserProducts(userId)
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        serviceProductViewModel.unassignEvent.collect { event ->
            when (event) {
                is UnassignEvent.Success -> {
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    // Aquí no solemos navegar, la lista se actualiza sola por el loadUserProducts
                }
                is UnassignEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val actionState by stripeViewModel.actionUiState.collectAsStateWithLifecycle()
    val refundState by stripeViewModel.refundUiState.collectAsStateWithLifecycle()
    var pendingRefundProductId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ActionUiState.Success -> {
                // Si Stripe confirma éxito, refrescamos la lista local
                serviceProductViewModel.loadUserProducts(userId)
                Toast.makeText(context, "Operación realizada con éxito", Toast.LENGTH_SHORT).show()
                stripeViewModel.resetActionState()
            }
            is ActionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                stripeViewModel.resetActionState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(refundState) {
        when (val state = refundState) {
            is RefundUiState.Success -> {
                val pendingId = pendingRefundProductId
                if (pendingId != null && pendingId == state.productId) {
                    serviceProductViewModel.unassignProductFromUser(state.productId, userId)
                }
                pendingRefundProductId = null
                stripeViewModel.resetRefundState()
            }
            is RefundUiState.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_LONG
                ).show()
                pendingRefundProductId = null
                stripeViewModel.resetRefundState()
            }
            else -> Unit
        }
    }

    // --- Gestión de la UI según el estado ---
    when (val state = productsUiState) {
        is UserProductsUiState.Loading -> {
            // Reutilizamos el Shimmer que creamos antes
            MyProductsShimmer()
        }
        is UserProductsUiState.Error -> {
            ErrorComponent(
                message = state.message,
                onRetry = { serviceProductViewModel.loadUserProducts(userId) }
            )
        }
        is UserProductsUiState.Success -> {
            Log.d("PRODUCTOS", "Productos cargados: ${state.products}")
            MyProductsContent(
                products = state.products, // Pasamos la lista real extraída del Success
                onProductClick = { product ->
                    serviceProductViewModel.selectedProduct = product
                    navController.navigate(ActiveProductDetail(product.id))
                },
                onConfirmCancel = { product ->
                    when {
                        // 1. Si es Suscripción
                        !product.stripeSubscriptionId.isNullOrBlank() -> {
                            stripeViewModel.cancelSubscription(
                                product.stripeSubscriptionId!!,
                                product.id,
                                userId
                            )
                        }
                        // 2. Si es Pago Único (Refund)
                        !product.stripePaymentIntentId.isNullOrBlank() -> {
                            pendingRefundProductId = product.id
                            stripeViewModel.createRefund(
                                product.stripePaymentIntentId!!,
                                product.id,
                                product.price
                            )
                        }
                        // 3. Si no tiene Stripe (Baja manual/local)
                        else -> {
                            serviceProductViewModel.unassignProductFromUser(product.id, userId)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MyProductsContent(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onConfirmCancel: (Product) -> Unit
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (products.isEmpty()) {
            Text(
                "No tienes productos contratados.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = products, key = { it.id }) { product ->
                    product.image?.let { Log.d("IMAGEN", it) }

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
                    onConfirmCancel(selectedProduct!!)
                    showCancelConfirmation = false
                    showOptionsDialog = false
                    selectedProduct = null
                },
                onDismiss = { showCancelConfirmation = false }
            )
        }
    }
}
