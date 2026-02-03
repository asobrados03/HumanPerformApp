package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.humanperformcenter.app.navigation.HireProduct
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.app.navigation.StripeCheckout
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.ui.ServiceProductUiState
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.ui.models.BillingPrefill
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.components.ServiceProductsShimmer
import com.humanperformcenter.shared.presentation.viewmodel.PaymentViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HireProductScreen(
    serviceId: Int,
    navController: NavHostController,
    serviceProductViewModel: ServiceProductViewModel,
    paymentViewModel: PaymentViewModel,
    userData: User?
) {
    val context = LocalContext.current
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // --- State Collection ---
    val productosMap by serviceProductViewModel.serviceProducts.collectAsStateWithLifecycle()
    // Si no hay estado en el mapa para este ID, asumimos Loading
    val estadoProductos = productosMap[serviceId] ?: ServiceProductUiState.Loading

    val productosContratados by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()
    val userCoupons by serviceProductViewModel.userCoupons.collectAsStateWithLifecycle()

    // Extraemos la lista de forma segura solo si el estado es Success
    val listaBase = remember(estadoProductos) {
        (estadoProductos as? ServiceProductUiState.Success)?.services ?: emptyList()
    }

    val idsContratados = remember(productosContratados) {
        if (productosContratados is UserProductsUiState.Success) {
            (productosContratados as UserProductsUiState.Success).products.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }
    val sesionesDisponibles = remember(listaBase) {
        listaBase.mapNotNull { it.session }.distinct().sorted()
    }

    // --- UI State Local ---
    var selectedFilter by remember { mutableStateOf(ProductTypeFilter.ALL) }
    var selectedSessionCount by remember { mutableIntStateOf(0) }
    var productoIdSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var cuponTexto by remember { mutableStateOf("") }

    // Productos filtrados a partir de la lista base del estado Success
    val productosFiltrados by remember(listaBase, selectedFilter, selectedSessionCount) {
        derivedStateOf {
            serviceProductViewModel.filterProducts(listaBase, selectedFilter, selectedSessionCount)
        }
    }

    LaunchedEffect(Unit) {
        serviceProductViewModel.assignEvent.collect { event ->
            when (event) {
                is AssignEvent.Success -> {
                    Toast.makeText(context, "Asignado con éxito", Toast.LENGTH_SHORT).show()

                    showPaymentSheet = false
                    productoIdSeleccionado = null

                    navController.navigate(ProductDetail(productId = event.productId)) {
                        popUpTo<HireProduct> { inclusive = true }
                    }
                }
                is AssignEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Handlers ---
    fun handleProductAssignment(productId: Int, method: String) {
        val uid = userData?.id
        if (uid != null) {
            serviceProductViewModel.assignProductToUser(
                userId = uid,
                productId = productId,
                paymentMethod = method,
                couponCode = cuponTexto.takeIf { it.isNotBlank() }
            )
        } else {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(serviceId, userData) {
        userData?.id?.let { uid ->
            serviceProductViewModel.loadServiceProducts(serviceId)
            serviceProductViewModel.loadUserProducts(uid)
            serviceProductViewModel.loadUserCoupons(uid)
        }
    }

    Scaffold(
        topBar = {
            Column {
                LogoAppBar(showBackArrow = true) { navController.popBackStack() }
                ProductFiltersSection(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    selectedSessionCount = selectedSessionCount,
                    onSessionChange = { selectedSessionCount = it },
                    sesionesDisponibles = sesionesDisponibles
                )
            }
        }
    ) { padding ->
        // Gestión de la navegación de estados
        when (estadoProductos) {
            is ServiceProductUiState.Loading -> {
                ServiceProductsShimmer(Modifier.padding(padding))
            }
            is ServiceProductUiState.Error -> {
                ErrorView(
                    message = estadoProductos.message,
                    modifier = Modifier.padding(padding),
                    onRetry = { serviceProductViewModel.loadServiceProducts(serviceId) }
                )
            }
            is ServiceProductUiState.Success -> {
                ProductList(
                    modifier = Modifier.padding(padding),
                    availableProducts = productosFiltrados,
                    idsContratados = idsContratados,
                    userCoupons = userCoupons,
                    onProductClick = { product ->
                        productoIdSeleccionado = product.id
                        showPaymentSheet = true
                    },
                    serviceProductViewModel = serviceProductViewModel
                )
            }
        }
    }

    // Diálogos y Sheets (Se mantienen igual, usando listaBase)
    if (showPaymentSheet && productoIdSeleccionado != null) {
        listaBase.firstOrNull { it.id == productoIdSeleccionado }?.let { product ->
            ModalBottomSheet(onDismissRequest = { showPaymentSheet = false }) {
                PaymentSelectionContent(
                    product = product,
                    userData = userData,
                    userCoupons = userCoupons,
                    cuponTexto = cuponTexto,
                    onCuponChange = { cuponTexto = it },
                    paymentViewModel = paymentViewModel,
                    navController = navController,
                    onMonederoPay = { handleProductAssignment(product.id, "cash") },
                    serviceProductViewModel = serviceProductViewModel,
                    onRebillSuccess = { /* Lógica rebill */ }
                )
            }
        }
    }
}

// --- Componentes de Apoyo ---

@Composable
private fun ErrorView(message: String, modifier: Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ocurrió un error", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Reintentar")
        }
    }
}

// -----------------------------------------------------------------------------
// SUB-COMPOSABLES (Para limpiar el código principal) que en un futuro
// estarán en archivos/composables independientes en ui/components
// -----------------------------------------------------------------------------

@Composable
fun ProductFiltersSection(
    selectedFilter: ProductTypeFilter,
    onFilterChange: (ProductTypeFilter) -> Unit,
    selectedSessionCount: Int,
    onSessionChange: (Int) -> Unit,
    sesionesDisponibles: List<Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Dropdown Tipo
        CustomDropdown(
            label = selectedFilter.label,
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            ProductTypeFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = { onFilterChange(filter); closeMenu() }
                )
            }
        }

        // Dropdown Sesiones
        CustomDropdown(
            label = if (selectedSessionCount == 0) "Todas las sesiones" else "$selectedSessionCount sesiones",
            modifier = Modifier.weight(1f)
        ) { closeMenu ->
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSessionChange(0); closeMenu() }
            )
            sesionesDisponibles.forEach { sesion ->
                DropdownMenuItem(
                    text = { Text("$sesion sesiones") },
                    onClick = { onSessionChange(sesion); closeMenu() }
                )
            }
        }
    }
}

@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    availableProducts: List<Product>,
    idsContratados: Set<Int>,
    userCoupons: List<Coupon>,
    onProductClick: (Product) -> Unit,
    serviceProductViewModel: ServiceProductViewModel,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (availableProducts.isEmpty()) {
            item {
                Text("No hay productos disponibles para este servicio.")
            }
        } else {
            items(
                items = availableProducts,
                key = { it.id }
            ) { availableProduct ->
                val contratado = idsContratados.contains(availableProduct.id)

                // Calculamos el precio aquí para usarlo en la tarjeta
                val precioFinal = remember(availableProduct, userCoupons) {
                    serviceProductViewModel.calcularPrecioConDescuento(
                        availableProduct.id,
                        availableProduct.price ?: 0.0,
                        userCoupons
                    )
                }

                AppCard(
                    onClick = {
                        if (!contratado) {
                            onProductClick(availableProduct)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Imagen
                        availableProduct.image?.let { imagePath ->
                            AsyncImage(
                                model = "${ApiClient.baseUrl}/product_images/$imagePath",
                                contentDescription = availableProduct.name,
                                modifier = Modifier
                                    .size(69.dp)
                                    .padding(end = 12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(availableProduct.name, style = MaterialTheme.typography.titleMedium)
                            if (contratado) {
                                Text(
                                    "Ya contratado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${precioFinal.toInt()}€",
                            fontWeight = if (contratado) FontWeight.Normal else FontWeight.Bold,
                            color = if (contratado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSelectionContent(
    product: Product,
    userData: User?,
    userCoupons: List<Coupon>,
    cuponTexto: String,
    onCuponChange: (String) -> Unit,
    paymentViewModel: PaymentViewModel,
    navController: NavHostController,
    onMonederoPay: () -> Unit,
    onRebillSuccess: () -> Unit,
    serviceProductViewModel: ServiceProductViewModel,
) {
    var showMonederoConfirmation by remember { mutableStateOf(false) }
    // Aquí puedes meter más lógica UI para mostrar/ocultar tarjetas guardadas
    // Simplificado para legibilidad

    Column(Modifier.padding(16.dp)) {
        Text("Selecciona método de pago", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        // Input cupón podría ir aquí
        // OutlinedTextField(value = cuponTexto, onValueChange = onCuponChange, label = { Text("Cupón") })

        val precioFinal = serviceProductViewModel.calcularPrecioConDescuento(
            product.id,
            product.price ?: 0.0,
            userCoupons
        )

        // 3. Stripe
        Button(
            onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("selected_product_id", product.id)
                    set("selected_coupon", cuponTexto.takeIf { it.isNotBlank() })
                }
                /*paymentViewModel.startStripeCheckout(
                    amountInCents = (precioFinal * 100).toInt(),
                    currency = "EUR",
                    userId = userData?.id ?: 0,
                    productId = product.id,
                    couponCode = cuponTexto.takeIf { it.isNotBlank() },
                    billing = BillingPrefill(
                        name = userData?.fullName,
                        email = userData?.email,
                        addressLine1 = userData?.postAddress,
                        postalCode = userData?.postcode?.toString(),
                        city = "Segovia"
                    )
                )*/
                navController.navigate(StripeCheckout)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Pagar con Stripe 💳")
        }
        Spacer(Modifier.height(8.dp))

        // 4. Monedero
        Button(
            onClick = { showMonederoConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Monedero virtual 👛")
        }
    }

    if (showMonederoConfirmation) {
        AlertDialog(
            onDismissRequest = { showMonederoConfirmation = false },
            title = { Text("Confirmar pago") },
            text = { Text("¿Pagar con saldo virtual?") },
            confirmButton = {
                TextButton(onClick = {
                    showMonederoConfirmation = false
                    onMonederoPay()
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showMonederoConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CustomDropdown(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable (closeMenu: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = true }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            content { expanded = false }
        }
    }
}
