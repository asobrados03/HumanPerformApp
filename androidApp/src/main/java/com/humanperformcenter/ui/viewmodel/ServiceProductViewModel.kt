package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.di.AppModule.userUseCase
import com.humanperformcenter.shared.data.model.Coupon
import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.data.model.ServiceUiModel
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _userCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    val userCoupons: StateFlow<List<Coupon>> = _userCoupons

    var productoSeleccionado: ServiceItem? = null

    val hireViewUiState: StateFlow<List<ServiceUiModel>> = combine(
        allServices,
        userProducts
    ) { services, products ->

        // Aquí ocurre la "magia" en un hilo secundario (IO/Default), no en el Main Thread
        services.map { service ->
            // Calculamos una sola vez si está contratado
            val isHired = products.any { product ->
                product.serviceIds.contains(service.id)
            }

            ServiceUiModel(
                service = service,
                isHired = isHired
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val productsState = _userProducts.map { list ->
        list.distinctBy { it.id }.map { item ->
            item.copy(image = "${ApiClient.baseUrl}/product_images/${item.image}")
        }
    }

    fun loadAllServices() {
        viewModelScope.launch(Dispatchers.IO) {
            val services = useCase.getAllServices()
            _allServices.value = services
        }
    }

    fun loadServiceProducts(serviceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val products = useCase.getServiceProducts(serviceId)
            _serviceProducts.value = _serviceProducts.value.toMutableMap().apply {
                put(serviceId, products)
            }
        }
    }

    fun loadUserProducts(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val products = useCase.getUserProducts(userId)
            _userProducts.value = products
            println("User products loaded: ${products.size} items")
        }
    }

    fun loadInitialData(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Cargar servicios
            loadAllServices()
            // 2. Cargar productos del usuario
            loadUserProducts(userId)

            // 3. (Opcional) Si necesitas cargar detalles de cada servicio, hazlo aquí,
            // no en la UI. Idealmente tu backend debería devolver todo junto.
            // Si es obligatorio hacerlo así:
            allServices.value.forEach { service ->
                loadServiceProducts(service.id)
            }
        }
    }

    fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val (success, error) = useCase.assignProductToUser(
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
            onResult(success, error)
        }
    }

    fun unassignProductFromUser(targetId: Int, userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = useCase.unassignProductFromUser(userId, targetId)
            if (success) {
                loadUserProducts(userId)
            } else {
                println("❌ Error al descontratar producto")
            }
        }
    }

    fun fetchProductDetails(userId: Int, productId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _productDetails.value = useCase.getProductDetails(userId, productId)
        }
    }

    fun aplicarCupon(codigo: String, userId: Int, productId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                /*val response = PaymentApi.initiatePayment(
                    customerId = userId,
                    productId = productoId,
                    billingStreet = "Calle de la prueba",
                    billingPostal = "40002",
                    email = "human2@mail.com"
                )
                onSuccess(response.paymentUrl)*/
            } catch (e: Exception) {
                println("❌ Error al obtener URL de pago: ${e.localizedMessage}")
                onError()
            }
        }
    }

    fun loadUserCoupons(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userUseCase.getUserCoupons(userId)
            result.onSuccess { _userCoupons.value = it }
        }
    }

}
