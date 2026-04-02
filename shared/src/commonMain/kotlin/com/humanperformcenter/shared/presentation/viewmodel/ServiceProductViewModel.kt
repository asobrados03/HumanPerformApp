package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import com.humanperformcenter.shared.presentation.ui.ActiveProductDetailState
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.ui.ProductDetailUiState
import com.humanperformcenter.shared.presentation.ui.ServiceProductUiState
import com.humanperformcenter.shared.presentation.ui.ServiceUiState
import com.humanperformcenter.shared.presentation.ui.UnassignEvent
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.shared.presentation.ui.models.ServiceUiModel
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import com.rickclephas.kmp.observableviewmodel.stateIn
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow

class ServiceProductViewModel(
    private val serviceProductUseCase: ServiceProductUseCase,
    private val userCouponUseCase: UserCouponUseCase
) : ViewModel() {
    companion object {
        val log = logging() // Uses class name as tag
    }

    private val _serviceUiState = MutableStateFlow<ServiceUiState>(ServiceUiState.Loading)
    val serviceUiState: StateFlow<ServiceUiState> = _serviceUiState.asStateFlow()

    private val _serviceProducts = MutableStateFlow<Map<Int, ServiceProductUiState>>(emptyMap())
    val serviceProducts: StateFlow<Map<Int, ServiceProductUiState>> = _serviceProducts.asStateFlow()

    private val _userProductsState = MutableStateFlow<UserProductsUiState>(UserProductsUiState.Loading)
    val userProductsState: StateFlow<UserProductsUiState> = _userProductsState.asStateFlow()

    private val _activeProductDetails = MutableStateFlow<ActiveProductDetailState>(ActiveProductDetailState.Loading)
    val activeProductDetails: StateFlow<ActiveProductDetailState> = _activeProductDetails.asStateFlow()

    private val _productDetailState = MutableStateFlow<ProductDetailUiState>(
        ProductDetailUiState.Idle
    )
    val productDetailState: StateFlow<ProductDetailUiState> = _productDetailState.asStateFlow()

    private val _userCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    val userCoupons: StateFlow<List<Coupon>> = _userCoupons.asStateFlow()

    private val _assignEvent = Channel<AssignEvent>()
    val assignEvent: Flow<AssignEvent> = _assignEvent.receiveAsFlow()

    private val _unassignEvent = Channel<UnassignEvent>()
    val unassignEvent: Flow<UnassignEvent> = _unassignEvent.receiveAsFlow()

    var selectedProduct: Product? = null

    val isAlreadyHired: StateFlow<Boolean> = combine(
        productDetailState,
        userProductsState
    ) { productState, userState ->
        // Lógica de combinación:
        if (productState is ProductDetailUiState.Success && userState is UserProductsUiState.Success) {
            // Buscamos si el ID del producto actual está en la lista del usuario
            userState.products.any { it.id == productState.product.id }
        } else {
            false
        }
    }.stateIn(
        viewModelScope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun loadServiceProducts(serviceId: Int, userId: Int) {
        viewModelScope.launch {
            // 1. Estado: CARGANDO
            updateState(serviceId, ServiceProductUiState.Loading)

            val result = serviceProductUseCase.getServiceProducts(serviceId, userId)

            // 2. Estado: ÉXITO o ERROR
            result.onSuccess { products ->
                updateState(serviceId, ServiceProductUiState.Success(products))
            }.onFailure { error ->
                updateState(serviceId, ServiceProductUiState.Error(error.message
                    ?: "Error desconocido")
                )
            }
        }
    }

    private fun updateState(serviceId: Int, newState: ServiceProductUiState) {
        _serviceProducts.value = _serviceProducts.value.toMutableMap().apply {
            put(serviceId, newState)
        }
    }

    fun loadUserProducts(userId: Int) {
        _userProductsState.value = UserProductsUiState.Loading

        viewModelScope.launch {
            val result = serviceProductUseCase.getUserProducts(userId)

            result.onSuccess { products ->
                _userProductsState.value = UserProductsUiState.Success(products)
                log.debug { "✅ Productos cargados: ${products.size}" }
            }.onFailure { error ->
                _userProductsState.value = UserProductsUiState.Error(error.message
                    ?: "Error desconocido"
                )
                log.error { "❌ Error cargando productos: ${error.message}" }
            }
        }
    }

    fun loadProductDetail(productId: Int) {
        _productDetailState.value = ProductDetailUiState.Loading

        viewModelScope.launch {
            val result = serviceProductUseCase.getProductDetailHireProduct(productId)

            result.onSuccess { product ->
                _productDetailState.value = ProductDetailUiState.Success(product = product)
            }.onFailure { error ->
                _productDetailState.value = ProductDetailUiState.Error(
                    error.message ?: "Error desconocido"
                )
            }
        }
    }

    fun loadAllServices(userId: Int) {
        viewModelScope.launch {
            _serviceUiState.value = ServiceUiState.Loading

            // Lanzamos ambas peticiones en paralelo para ganar velocidad
            val servicesDeferred = async { serviceProductUseCase.getAllServices() }
            val productsResult = async { serviceProductUseCase.getUserProducts(userId) }

            val servicesResponse = servicesDeferred.await()
            val productsResponse = productsResult.await()

            // Procesamos solo si ambos tienen éxito, o manejamos el error
            _serviceUiState.value = servicesResponse.fold(
                onSuccess = { services ->
                    val hiredProducts = productsResponse.getOrDefault(emptyList())

                    val models = services.map { s ->
                        ServiceUiModel(
                            service = s,
                            // Verificamos si algún producto contratado pertenece a este servicio
                            isHired = hiredProducts.any { product ->
                                product.id == s.id // O la lógica de IDs que uses
                            }
                        )
                    }
                    ServiceUiState.Success(models)
                },
                onFailure = {
                    ServiceUiState.Error(it.message ?: "Error al cargar servicios")
                }
            )
        }
    }

    fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null
    ) {
        viewModelScope.launch {
            val result = serviceProductUseCase.assignProductToUser(
                userId, productId, paymentMethod, couponCode
            )

            result.fold(
                onSuccess = {
                    loadUserProducts(userId)
                    _assignEvent.send(AssignEvent.Success(productId))
                },
                onFailure = { error ->
                    _assignEvent.send(AssignEvent.Error(error.message ?: "Error"))
                }
            )
        }
    }

    fun unassignProductFromUser(productId: Int, userId: Int) {
        viewModelScope.launch {
            val result = serviceProductUseCase.unassignProductFromUser(userId, productId)

            result.fold(
                onSuccess = {
                    loadUserProducts(userId)
                    _unassignEvent.send(UnassignEvent.Success)
                },
                onFailure = { error ->
                    _unassignEvent.send(
                        UnassignEvent.Error(error.message
                            ?: "No se pudo eliminar")
                    )
                }
            )
        }
    }

    fun fetchActiveProductDetail(userId: Int, productId: Int) {
        _activeProductDetails.value = ActiveProductDetailState.Loading

        viewModelScope.launch {
            val result = serviceProductUseCase.getActiveProductDetail(userId, productId)

            result.onSuccess { productDetail ->
                _activeProductDetails.value = ActiveProductDetailState.Success(productDetail)
            }.onFailure { throwable ->
                val errorMsg = throwable.message ?: "Error al cargar el producto"
                _activeProductDetails.value = ActiveProductDetailState.Error(errorMsg)
            }
        }
    }

    fun loadUserCoupons(userId: Int) {
        viewModelScope.launch {
            val result = userCouponUseCase.getUserCoupons(userId)
            result.onSuccess { _userCoupons.value = it }
        }
    }

    fun filterProducts(
        list: List<Product>,
        filter: ProductTypeFilter,
        sessionCount: Int
    ): List<Product> {
        return serviceProductUseCase.filterProducts(list, filter, sessionCount)
    }

    fun calculateDiscountedPrice(productId: Int, originalPrice: Double, coupons: List<Coupon>)
            : Double {
        return serviceProductUseCase.calculateDiscountedPrice(productId, originalPrice, coupons)
    }
}
