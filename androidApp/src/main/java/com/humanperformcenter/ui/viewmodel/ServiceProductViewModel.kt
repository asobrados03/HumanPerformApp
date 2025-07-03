package com.humanperformcenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.ServicioItembien
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceProductViewModel(
    private val useCase: ServiceProductUseCase
) : ViewModel() {

    private val _allServices = MutableStateFlow<List<ServicioDispo>>(emptyList())
    val allServices: StateFlow<List<ServicioDispo>> get() = _allServices

    private val _serviceProducts = MutableStateFlow<Map<Int, List<ServicioItembien>>>(emptyMap())
    val serviceProducts: StateFlow<Map<Int, List<ServicioItembien>>> get() = _serviceProducts

    private val _userProducts = MutableStateFlow<List<ServicioItembien>>(emptyList())
    val userProducts: StateFlow<List<ServicioItembien>> get() = _userProducts

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
        }
    }
}
