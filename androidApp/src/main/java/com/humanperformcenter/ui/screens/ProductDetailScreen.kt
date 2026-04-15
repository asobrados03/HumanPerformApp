package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.shared.data.network.API_BASE_URL
import com.humanperformcenter.shared.presentation.ui.ProductDetailUiState
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.hire_product.ErrorView
import com.humanperformcenter.ui.components.hire_product.PaymentSelectionContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavHostController,
    serviceProductViewModel: ServiceProductViewModel,
    userId: Int
) {
    val context = LocalContext.current

    // --- Args de navegación ---
    val backStackEntry = remember(navController) {
        navController.currentBackStackEntry
    }
    val productId = backStackEntry?.arguments?.getInt("productId") ?: return

    // --- State (Observando la "Fuente de Verdad" del ViewModel) ---
    val productState by serviceProductViewModel.productDetailState.collectAsStateWithLifecycle()
    val userCoupons by serviceProductViewModel.userCoupons.collectAsStateWithLifecycle()

    // Aquí recibimos el booleano mágico que ya sabe si está contratado o no
    val isAlreadyHired by serviceProductViewModel.isAlreadyHired.collectAsStateWithLifecycle()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }

    fun handleProductAssignment(productId: Int, method: String) {
        if (userId != -1) {
            serviceProductViewModel.assignProductToUser(
                userId = userId,
                productId = productId,
                paymentMethod = method,
                couponCode = couponCode.takeIf { it.isNotBlank() }
            )
        } else {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Cargas ---
    LaunchedEffect(productId, userId) {
        serviceProductViewModel.loadProductDetail(productId)
        serviceProductViewModel.loadUserCoupons(userId)
        serviceProductViewModel.loadUserProducts(userId)
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        }
    ) { padding ->

        when (val state = productState) {
            is ProductDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ProductDetailUiState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = Modifier.padding(padding),
                    onRetry = { serviceProductViewModel.loadProductDetail(productId) }
                )
            }

            is ProductDetailUiState.Success -> {
                val product = state.product

                val appliedCoupon = remember(product, userCoupons) {
                    userCoupons
                        // 1. Filtramos cupones válidos para este producto (misma lógica que el cálculo)
                        .filter { it.productIds.isEmpty() || it.productIds.contains(product.id) }
                        // 2. Buscamos el que daría el mayor descuento
                        .maxByOrNull { coupon ->
                            if (coupon.isPercentage) (product.price ?: 0.0) * coupon.discount / 100
                            else coupon.discount
                        }
                }

                LaunchedEffect(appliedCoupon) {
                    couponCode = appliedCoupon?.code ?: ""
                }

                // Ya no necesitamos el remember { ... } aquí, usamos isAlreadyHired del ViewModel
                val finalPrice = remember(product, userCoupons) {
                    serviceProductViewModel.calculateDiscountedPrice(
                        product.id,
                        product.price ?: 0.0,
                        userCoupons
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- Imagen ---
                    product.image?.let { imagePath ->
                        AsyncImage(
                            model = "${API_BASE_URL}/product_images/$imagePath",
                            contentDescription = product.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // --- Título y Descripción ---
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(product.description ?: "No hay descripción disponible.", style = MaterialTheme.typography.bodyMedium)

                    // --- Etiquetas de Tipo ---
                    val productTypeLabel = when (product.typeOfProduct) {
                        "recurrent" -> "Suscripción"
                        "multi_sessions" -> "Bono de sesiones"
                        "single_session" -> "Sesión individual"
                        else -> product.typeOfProduct?.replaceFirstChar(Char::uppercase)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (product.typeOfProduct != "single_session" && product.session != null) {
                            Text("Sesiones: ${product.session}")
                        }
                        productTypeLabel?.let { label ->
                            Text(text = "Tipo: $label", fontWeight = FontWeight.Bold)
                        }
                    }

                    // --- Precio ---
                    Text(
                        text = "Precio: ${finalPrice.toInt()}€",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )

                    // --- Lógica del Botón y Mensaje ---
                    if (isAlreadyHired) {
                        Text(
                            text = "Ya has contratado este producto.",
                            color = MaterialTheme.colorScheme.secondary, // Cambiado a secondary para resaltar
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { showPaymentSheet = true },
                            enabled = !isAlreadyHired, // Reactivo al combine del ViewModel
                            modifier = Modifier.testTag("service_product_buy_cta")
                        ) {
                            Text(if (isAlreadyHired) "Producto adquirido" else "Comprar")
                        }
                    }
                }
            }
            else -> {}
        }
    }

    // --- Payment sheet ---
    if (showPaymentSheet && productState is ProductDetailUiState.Success) {
        ModalBottomSheet(onDismissRequest = { showPaymentSheet = false }) {
            PaymentSelectionContent(
                product = (productState as ProductDetailUiState.Success).product,
                userCoupons = userCoupons,
                couponCode = couponCode,
                navController = navController,
                serviceProductViewModel = serviceProductViewModel,
                onElectronicWalletPayment = {
                    handleProductAssignment((productState as ProductDetailUiState.Success).product.id, "cash")
                },
            )
        }
    }
}
