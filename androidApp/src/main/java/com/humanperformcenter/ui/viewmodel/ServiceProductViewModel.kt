package com.humanperformcenter.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.humanperformcenter.shared.data.network.PaymentApi

class ServiceProductViewModel(
    private val useCase: ServiceProductUseCase
) : ViewModel() {

    private val _allServices = MutableStateFlow<List<ServiceAvailable>>(emptyList())
    val allServices: StateFlow<List<ServiceAvailable>> get() = _allServices

    private val _serviceProducts = MutableStateFlow<Map<Int, List<ServiceItem>>>(emptyMap())
    val serviceProducts: StateFlow<Map<Int, List<ServiceItem>>> get() = _serviceProducts

    private val _userProducts = MutableStateFlow<List<ServiceItem>>(emptyList())
    val userProducts: StateFlow<List<ServiceItem>> get() = _userProducts

    private val _productDetails = MutableStateFlow<ProductDetailResponse?>(null)
    val productDetails: StateFlow<ProductDetailResponse?> get() = _productDetails

    var productoSeleccionado: ServiceItem? = null

    fun loadAllServices() {
        viewModelScope.launch {
            val services = useCase.getAllServices()
            _allServices.value = services
        }
    }

    fun loadServiceProducts(serviceId: Int) {
        viewModelScope.launch {
            val products = useCase.getServiceProducts(serviceId)
            _serviceProducts.value = _serviceProducts.value.toMutableMap().apply {
                put(serviceId, products)
            }
        }
    }

    fun loadUserProducts(userId: Int) {
        viewModelScope.launch {
            val products = useCase.getUserProducts(userId)
            _userProducts.value = products
            println("User products loaded: ${products.size} items")
        }
    }

    fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = useCase.assignProductToUser(
                userId = userId,
                productId = productId,
                paymentMethod = paymentMethod,
                couponCode = couponCode,
            )
            if (success) {
                println("✅ Producto asignado correctamente")
                loadUserProducts(userId)
            } else {
                println("❌ Error al asignar producto")
            }
            onResult(success)
        }
    }

    fun unassignProductFromUser(productId: Int, userId: Int) {
        viewModelScope.launch {
            val success = useCase.unassignProductFromUser(userId, productId)
            if (success) {
                loadUserProducts(userId)
            } else {
                println("❌ Error al descontratar producto")
            }
        }
    }

    fun fetchProductDetails(userId: Int, productId: Int) {
        viewModelScope.launch {
            _productDetails.value = useCase.getProductDetails(userId, productId)
        }
    }

    fun aplicarCupon(codigo: String, userId: Int, productId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = useCase.applyCoupon(codigo, userId, productId)
            onResult(success)
        }
    }


    fun getPaymentUrl(
        productoId: Int,
        userId: Int,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = PaymentApi.initiatePayment(
                    customerId = userId,
                    productId = productoId,
                    billingStreet = "Calle de la prueba",
                    billingPostal = "40002",
                    email = "human2@mail.com"
                )
                onSuccess(response.paymentUrl)
            } catch (e: Exception) {
                println("❌ Error al obtener URL de pago: ${e.localizedMessage}")
                onError()
            }
        }
    }

}
