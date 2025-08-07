package com.humanperformcenter.ui.screens

import android.provider.ContactsContract
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton
import com.humanperformcenter.app.navigation.StartPayment
import com.humanperformcenter.shared.data.model.Coupon
import com.humanperformcenter.shared.data.model.PaymentRequest
import com.humanperformcenter.ui.components.AppCard
import com.humanperformcenter.ui.components.LogoAppBar
import com.humanperformcenter.ui.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.state.PaymentState
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HireProductScreen(
    serviceId: Int,
    navController: NavHostController,
    viewModel: ServiceProductViewModel,
    userId: Int,
    paymentViewModel: PaymentViewModel,
    userEmail: String
) {
    val context = LocalContext.current

    val paymentState by paymentViewModel.paymentState.collectAsState()

    var showMonederoConfirmation by remember { mutableStateOf(false) }


    // 2) Observamos el estado para toasts y navegación
    LaunchedEffect(paymentState) {
        when (paymentState) {
            PaymentState.Loading -> Toast.makeText(context,"Iniciando…",Toast.LENGTH_SHORT).show()
            is PaymentState.Success -> {
                Toast.makeText(context,"Pago OK",Toast.LENGTH_SHORT).show()
                navController.navigate("startPayment")
            }
            is PaymentState.Error -> {
                val msg = (paymentState as PaymentState.Error).message
                Toast.makeText(context,"Error: $msg",Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // --- Screen State ---
    val productosMap by viewModel.serviceProducts.collectAsState()
    val productos = productosMap[serviceId] ?: emptyList()

    val productosContratados by viewModel.userProducts.collectAsState()
    val idsContratados = productosContratados.map { it.id }.toSet()

    var mostrarCuponSheet by remember { mutableStateOf(false) }
    var productoIdSeleccionado by remember { mutableStateOf<Int?>(null) }
    var cuponTexto by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    var selectedFilter by remember { mutableStateOf(ProductTypeFilter.ALL) }

    var selectedSessionCount by remember { mutableIntStateOf(0) } // 0 = sin filtro
    val sesionesDisponibles = productos.mapNotNull { it.session }.distinct().sorted()
    val productosFiltrados by remember(productos, selectedFilter, selectedSessionCount) {
        derivedStateOf {
            productos.filter { producto ->
                val tipoOk = when (selectedFilter) {
                    ProductTypeFilter.RECURRENT -> producto.tipo_producto == "recurrent"
                    ProductTypeFilter.NON_RECURRENT -> producto.tipo_producto != "recurrent"
                    ProductTypeFilter.ALL -> true
                }
                val sesionesOk = if (selectedSessionCount == 0) true
                else producto.session == selectedSessionCount

                tipoOk && sesionesOk
            }
        }
    }

    LaunchedEffect(serviceId) {
        viewModel.loadServiceProducts(serviceId)
        viewModel.loadUserProducts(userId)
        viewModel.loadUserCoupons(userId)
        productos.forEach {
            println("Producto: ${it.name}, tipo: ${it.tipo_producto}")
        }
    }

    Scaffold(
        topBar = {
            Column {
                LogoAppBar(showBackArrow = true) {
                    navController.popBackStack()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dropdown de tipo de producto
                    var tipoExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { tipoExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedFilter.label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = tipoExpanded,
                            onDismissRequest = { tipoExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            ProductTypeFilter.entries.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter.label) },
                                    onClick = {
                                        selectedFilter = filter
                                        tipoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown de sesiones
                    var sesionesExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { sesionesExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedSessionCount == 0) "Todas las sesiones" else "$selectedSessionCount sesiones",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = sesionesExpanded,
                            onDismissRequest = { sesionesExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = {
                                    selectedSessionCount = 0
                                    sesionesExpanded = false
                                }
                            )
                            sesionesDisponibles.forEach { sesion ->
                                DropdownMenuItem(
                                    text = { Text("$sesion sesiones") },
                                    onClick = {
                                        selectedSessionCount = sesion
                                        sesionesExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp)
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (productosFiltrados.isEmpty()) {
                item {
                    Text("No hay productos disponibles para este servicio.")
                }
            } else items(productosFiltrados) { producto ->
                val contratado = idsContratados.contains(producto.id)
                AppCard(onClick = {
                    if (!contratado) {
                        productoIdSeleccionado = producto.id
                        mostrarCuponSheet = true
                    }
                }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        producto.image?.let {
                            AsyncImage(
                                model = "https://apihuman.fransdata.com/api/product_images/$it",
                                contentDescription = producto.name,
                                modifier = Modifier
                                    .size(69.dp)
                                    .padding(end = 12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(producto.name, style = MaterialTheme.typography.titleMedium)
                            if (contratado) {
                                Text(
                                    "Ya contratado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        val precioFinal = calcularPrecioConDescuento(
                            producto.id,
                            producto.price ?: 0.0,
                            viewModel.userCoupons.collectAsState().value
                        )
                        Text(
                            "${precioFinal.toInt()}€",
                            fontWeight = if (contratado) FontWeight.Normal else FontWeight.Bold,
                            color = if (contratado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // --- Bottom Sheet ---
    if (mostrarCuponSheet && productoIdSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarCuponSheet = false;
                productoIdSeleccionado = null; cuponTexto = ""
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.padding(16.dp)) {
                    Text("Selecciona método de pago", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    // — Google Pay Button —
                    PayButton(
                        modifier = Modifier.fillMaxWidth(),
                        type = ButtonType.Pay,
                        allowedPaymentMethods = allowedPaymentMethodsJson,
                        onClick = {
                            val requestJson = buildPaymentRequestJson(
                                precio = productos.first { it.id==productoIdSeleccionado }.price!!
                            )
                            paymentViewModel.payWithGooglePay(requestJson)
                        }
                    )
                    Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        val selectedProduct = productos.first { it.id == productoIdSeleccionado }
                        val paymentRequest = PaymentRequest(amount = 10, currency = "EUR")
                        paymentViewModel.generatePaymentURL(paymentRequest)
                        navController.navigate(StartPayment)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5), // azul tarjeta
                        contentColor = Color.White
                    )
                ) {
                    Text("Pagar con tarjeta 💳", fontSize = 16.sp)
                }
                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        showMonederoConfirmation = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        contentColor = Color.White
                    )
                ) {
                    Text("Pagar con monedero virtual 👛", fontSize = 16.sp)
                }
            }
        }
    }
    if (showMonederoConfirmation) {
        AlertDialog(
            onDismissRequest = { showMonederoConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMonederoConfirmation = false

                        viewModel.assignProductToUser(
                            userId,
                            productoIdSeleccionado!!,
                            "cash",
                            cuponTexto.takeIf { it.isNotBlank() }
                        ) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Producto asignado", Toast.LENGTH_SHORT).show()
                            } else {
                                errorMessage = error ?: "Error al asignar producto"
                            }
                        }

                        mostrarCuponSheet = false
                        productoIdSeleccionado = null
                        cuponTexto = ""
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMonederoConfirmation = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Confirmar pago con monedero") },
            text = { Text("¿Estás seguro de que deseas pagar este producto usando tu saldo virtual?") }
        )
    }

}



enum class ProductTypeFilter(val label: String) {
    RECURRENT("Recurrente"),
    NON_RECURRENT("No recurrente"),
    ALL("Todos")
}

// --- Helpers ---

private val allowedPaymentMethodsJson = """
[{
  "type":"CARD",
  "parameters":{
    "allowedAuthMethods":["PAN_ONLY","CRYPTOGRAM_3DS"],
    "allowedCardNetworks":["VISA","MASTERCARD","AMEX"]
  },
  "tokenizationSpecification":{
    "type":"PAYMENT_GATEWAY",
    "parameters":{
      "gateway":"globalpayments",
      "gatewayMerchantId":"TU_MERCHANT_ID"
    }
  }
}]
""".trimIndent()

/**
 * Construye el JSON de PaymentDataRequest según Google Pay API
 * @param precio total a cobrar (número decimal)
 */
private fun buildPaymentRequestJson(precio: Double): String {
    // Formateamos el precio con Locale.US para evitar bugs de coma/punto
    val precioStr = String.format(Locale.US, "%.2f", precio)

    val transactionInfo = JSONObject().apply {
        put("totalPrice", precioStr)
        put("totalPriceStatus", "FINAL")
        put("currencyCode", "EUR")
        put("countryCode", "ES")
    }
    val merchantInfo = JSONObject().apply {
        put("merchantName", "MiApp")
        put("merchantId", "TU_GOOGLE_PAY_MERCHANT_ID") // Opcional si ya lo tienes en Google Console
    }
    return JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods", JSONArray(allowedPaymentMethodsJson))
        put("transactionInfo", transactionInfo)
        put("merchantInfo", merchantInfo)
        // Pedimos email al usuario (no enviamos userEmail explícito)
        put("emailRequired", true)
        // No pedimos dirección de envío
        put("shippingAddressRequired", false)
    }.toString()
}

private fun calcularPrecioConDescuento(productoId: Int, precioOriginal: Double, cupones: List<Coupon>): Double {
    val descuentos = cupones.filter { cupon -> cupon.productIds.isEmpty() || cupon.productIds.contains(productoId) }.map { cupon ->
        if (cupon.isPercentage) precioOriginal * cupon.discount / 100
        else cupon.discount
    }
    val mayorDescuento = descuentos.maxOrNull() ?: 0.0
    return (precioOriginal - mayorDescuento).coerceAtLeast(0.0)
}
