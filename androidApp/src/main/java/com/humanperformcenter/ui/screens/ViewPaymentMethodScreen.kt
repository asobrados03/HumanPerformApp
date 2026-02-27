package com.humanperformcenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.shared.data.model.payment.StripePaymentMethod
import com.humanperformcenter.shared.presentation.ui.PaymentMethodsUiState
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.ui.components.app.ErrorComponent
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.app.rememberShimmerBrush

@Composable
fun ViewPaymentMethodScreen(
    navController: NavHostController,
    stripeViewModel: StripeViewModel
) {
    val uiState by stripeViewModel.viewPaymentMethodsUiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDeleteCardId by remember { mutableStateOf<String?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val normalizedCardNumber = cardNumber.filter(Char::isDigit)
    val normalizedExpiryDate = expiryDate.filter { it.isDigit() || it == '/' }
    val normalizedCvv = cvv.filter(Char::isDigit)

    val canSaveCard = normalizedCardNumber.length in 13..19 &&
        Regex("^(0[1-9]|1[0-2])/\\d{2}$").matches(normalizedExpiryDate) &&
        normalizedCvv.length in 3..4

    LaunchedEffect(Unit) {
        stripeViewModel.loadPaymentMethods()
    }

    Scaffold(
        topBar = {
            LogoAppBar(
                showBackArrow = true,
                onBackNavClicked = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Tus métodos de pago",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            when (val state = uiState) {
                is PaymentMethodsUiState.Loading -> PaymentMethodsShimmer()

                is PaymentMethodsUiState.Error -> {
                    ErrorComponent(
                        message = state.message,
                        onRetry = { stripeViewModel.loadPaymentMethods() }
                    )
                }

                is PaymentMethodsUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No hay métodos todavía",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Añade tu primera tarjeta para pagar más rápido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Outlined.AddCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir método de pago")
                        }
                    }
                }

                is PaymentMethodsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.paymentMethods, key = { it.id }) { pm ->
                            PaymentMethodCard(
                                paymentMethod = pm,
                                isDefault = pm.id == state.defaultPaymentMethodId,
                                onDelete = { pendingDeleteCardId = pm.id },
                                onSetDefault = { stripeViewModel.setDefaultCard(pm.id) }
                            )
                        }
                        item {
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.AddCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Añadir método de pago")
                            }
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Añadir método de pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = {
                            cardNumber = it.filter(Char::isDigit).take(19)
                        },
                        label = { Text("Número de tarjeta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {
                            val digits = it.filter(Char::isDigit).take(4)
                            expiryDate = when {
                                digits.length <= 2 -> digits
                                else -> "${digits.take(2)}/${digits.drop(2)}"
                            }
                        },
                        label = { Text("Fecha de expiración (MM/YY)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = {
                            cvv = it.filter(Char::isDigit).take(4)
                        },
                        label = { Text("CVV") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canSaveCard,
                    onClick = {
                        val paymentMethodId = "pm_manual_${normalizedCardNumber.takeLast(4)}_${normalizedExpiryDate.replace("/", "")}"
                        stripeViewModel.saveCard(paymentMethodId)
                        cardNumber = ""
                        expiryDate = ""
                        cvv = ""
                        showAddDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    cardNumber = ""
                    expiryDate = ""
                    cvv = ""
                    showAddDialog = false
                }) { Text("Cancelar") }
            }
        )
    }

    pendingDeleteCardId?.let { cardId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCardId = null },
            title = { Text("Eliminar método") },
            text = { Text("¿Seguro que deseas eliminar este método de pago?") },
            confirmButton = {
                TextButton(onClick = {
                    stripeViewModel.deleteCard(cardId)
                    pendingDeleteCardId = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCardId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PaymentMethodCard(
    paymentMethod: StripePaymentMethod,
    isDefault: Boolean,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    val cardDetails = paymentMethod.card
    val brand = (cardDetails.displayBrand ?: cardDetails.brand).uppercase()
    val last4 = cardDetails.last4.takeIf { it.isNotBlank() } ?: "••••"
    val expMonth = "%02d".format(cardDetails.expMonth)
    val expYear = cardDetails.expYear.toString().takeLast(2)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrandAvatar(brand = cardDetails.brand)
                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•••• $last4  ·  Expira $expMonth/$expYear",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isDefault) {
                    DefaultChip()
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isDefault) {
                    OutlinedButton(onClick = onSetDefault) {
                        Icon(Icons.Filled.Star, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Predeterminada")
                    }
                }
                OutlinedButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun BrandAvatar(brand: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = CircleShape,
        modifier = Modifier.size(44.dp),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = brand.take(2),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DefaultChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Predeterminado",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PaymentMethodsShimmer() {
    val shimmerBrush = rememberShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .background(shimmerBrush, RoundedCornerShape(20.dp))
            )
        }
    }
}
