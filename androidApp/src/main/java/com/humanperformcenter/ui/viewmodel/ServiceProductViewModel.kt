package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun assignProductToUser(userId: Int, productId: Int) {
        viewModelScope.launch {
            val success = useCase.assignProductToUser(userId, productId)
            if (success) {
                println("✅ Producto asignado correctamente")
                loadUserProducts(userId)
            } else {
                println("❌ Error al asignar producto")
            }
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
}
