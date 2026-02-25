package com.humanperformcenter.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.humanperformcenter.app.navigation.HireProduct
import com.humanperformcenter.app.navigation.ActiveProductDetail
import com.humanperformcenter.app.navigation.ProductDetail
import com.humanperformcenter.shared.data.model.user.User
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.ui.ServiceProductUiState
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.ui.components.service.ServiceProductsShimmer
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.components.app.LogoAppBar
import com.humanperformcenter.ui.components.hire_product.ErrorView
import com.humanperformcenter.ui.components.hire_product.ProductFiltersSection
import com.humanperformcenter.ui.components.hire_product.ProductList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HireProductScreen(
    serviceId: Int,
    navController: NavHostController,
    serviceProductViewModel: ServiceProductViewModel,
    userData: User?
) {
    val context = LocalContext.current

    // --- State Collection ---
    val productsMap by serviceProductViewModel.serviceProducts.collectAsStateWithLifecycle()
    // Si no hay estado en el mapa para este ID, asumimos Loading
    val productsState = productsMap[serviceId] ?: ServiceProductUiState.Loading

    val hiredProductsState by serviceProductViewModel.userProductsState.collectAsStateWithLifecycle()
    val userCoupons by serviceProductViewModel.userCoupons.collectAsStateWithLifecycle()

    // Extraemos la lista de forma segura solo si el estado es Success
    val serviceList = remember(productsState) {
        (productsState as? ServiceProductUiState.Success)?.services ?: emptyList()
    }

    val hiredIds = remember(hiredProductsState) {
        if (hiredProductsState is UserProductsUiState.Success) {
            (hiredProductsState as UserProductsUiState.Success).products.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }
    val availableSessions = remember(serviceList) {
        serviceList.mapNotNull { it.session }.distinct().sorted()
    }

    // --- UI State Local ---
    var selectedFilter by remember { mutableStateOf(ProductTypeFilter.ALL) }
    var selectedSessionCount by remember { mutableIntStateOf(0) }

    // Productos filtrados a partir de la lista base del estado Success
    val filteredProducts by remember(serviceList, selectedFilter, selectedSessionCount) {
        derivedStateOf {
            serviceProductViewModel.filterProducts(serviceList, selectedFilter, selectedSessionCount)
        }
    }

    LaunchedEffect(Unit) {
        serviceProductViewModel.assignEvent.collect { event ->
            when (event) {
                is AssignEvent.Success -> {
                    Toast.makeText(context, "Asignado con éxito", Toast.LENGTH_SHORT).show()

                    navController.navigate(ActiveProductDetail(productId = event.productId)) {
                        popUpTo<HireProduct> { inclusive = true }
                    }
                }
                is AssignEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
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
                    sesionesDisponibles = availableSessions
                )
            }
        }
    ) { padding ->
        // Gestión de la navegación de estados
        when (productsState) {
            is ServiceProductUiState.Loading -> {
                ServiceProductsShimmer(Modifier.padding(padding))
            }
            is ServiceProductUiState.Error -> {
                ErrorView(
                    message = productsState.message,
                    modifier = Modifier.padding(padding),
                    onRetry = { serviceProductViewModel.loadServiceProducts(serviceId) }
                )
            }
            is ServiceProductUiState.Success -> {
                ProductList(
                    modifier = Modifier.padding(padding),
                    availableProducts = filteredProducts,
                    idsContratados = hiredIds,
                    userCoupons = userCoupons,
                    onProductClick = { product ->
                        navController.navigate(ProductDetail(productId = product.id))
                    },
                    serviceProductViewModel = serviceProductViewModel
                )
            }
        }
    }
}
