package com.humanperformcenter.shared.presentation.viewmodel

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.presentation.ui.AssignEvent
import com.humanperformcenter.shared.presentation.ui.CouponEvent
import com.humanperformcenter.shared.presentation.ui.ProductDetailState
import com.humanperformcenter.shared.presentation.ui.ServiceProductUiState
import com.humanperformcenter.shared.presentation.ui.ServiceUiState
import com.humanperformcenter.shared.presentation.ui.UnassignEvent
import com.humanperformcenter.shared.presentation.ui.UserProductsUiState
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.shared.presentation.ui.models.ServiceUiModel
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.rickclephas.kmp.observableviewmodel.ViewModel
import com.rickclephas.kmp.observableviewmodel.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ServiceProductViewModel(
    private val useCase: ServiceProductUseCase,
    private val userUseCase: UserUseCase
) : ViewModel() {
    companion object {
        val log = logging() // Uses class name as tag
    }

    private val _serviceUiState = MutableStateFlow<ServiceUiState>(ServiceUiState.Loading)
    @NativeCoroutinesState
    val serviceUiState: StateFlow<ServiceUiState> = _serviceUiState.asStateFlow()

    private val _serviceProducts = MutableStateFlow<Map<Int, ServiceProductUiState>>(emptyMap())
    @NativeCoroutinesState
    val serviceProducts: StateFlow<Map<Int, ServiceProductUiState>> = _serviceProducts.asStateFlow()

    private val _userProductsState = MutableStateFlow<UserProductsUiState>(UserProductsUiState.Loading)
    @NativeCoroutinesState
    val userProductsState: StateFlow<UserProductsUiState> = _userProductsState.asStateFlow()

    private val _productDetailsState = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    @NativeCoroutinesState
    val productDetailsState: StateFlow<ProductDetailState> get() = _productDetailsState

    private val _userCoupons = MutableStateFlow<List<Coupon>>(emptyList())
    @NativeCoroutinesState
    val userCoupons: StateFlow<List<Coupon>> = _userCoupons

    private val _assignEvent = Channel<AssignEvent>()
    @NativeCoroutinesState
    val assignEvent = _assignEvent.receiveAsFlow()

    private val _unassignEvent = Channel<UnassignEvent>()
    @NativeCoroutinesState
    val unassignEvent = _unassignEvent.receiveAsFlow()

    var productoSeleccionado: Product? = null

    private val _couponEvent = Channel<CouponEvent>()
    @NativeCoroutinesState
    val couponEvent = _couponEvent.receiveAsFlow()

    fun loadServiceProducts(serviceId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Estado: CARGANDO
            updateState(serviceId, ServiceProductUiState.Loading)

            val result = useCase.getServiceProducts(serviceId)

            // 2. Estado: ÉXITO o ERROR
            result.onSuccess { products ->
                updateState(serviceId, ServiceProductUiState.Success(products))
            }.onFailure { error ->
                updateState(serviceId, ServiceProductUiState.Error(error.message ?: "Error desconocido"))
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

        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.getUserProducts(userId)

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

    fun loadAllServices(userId: Int) {
        viewModelScope.launch {
            _serviceUiState.value = ServiceUiState.Loading

            // Lanzamos ambas peticiones en paralelo para ganar velocidad
            val servicesDeferred = async { useCase.getAllServices() }
            val productsResult = async { useCase.getUserProducts(userId) } // userId desde donde lo tengas

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
                onFailure = { ServiceUiState.Error(it.message ?: "Error al cargar servicios") }
            )
        }
    }

    fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.assignProductToUser(
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
        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.unassignProductFromUser(userId, productId)

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

    fun fetchProductDetails(userId: Int, productId: Int) {
        _productDetailsState.value = ProductDetailState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = useCase.getProductDetails(userId, productId)

            result.onSuccess { productDetail ->
                _productDetailsState.value = ProductDetailState.Success(productDetail)
            }.onFailure { throwable ->
                val errorMsg = throwable.message ?: "Error al cargar el producto"
                _productDetailsState.value = ProductDetailState.Error(errorMsg)
            }
        }
    }

    fun aplicarCupon(codigo: String, userId: Int, productId: Int) {
        viewModelScope.launch {
            val result = useCase.applyCoupon(codigo, userId, productId)

            result.fold(
                onSuccess = {
                    // El cupón es válido
                    _couponEvent.send(CouponEvent.Success)
                },
                onFailure = { error ->
                    // Enviamos el mensaje de error para mostrar en el Toast/UI
                    _couponEvent.send(CouponEvent.Error(error.message ?: "Error desconocido"))
                }
            )
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
                println("❌ Error al obtener URL de pago: ${e.message}")
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

    fun filterProducts(
        list: List<Product>,
        filter: ProductTypeFilter,
        sessionCount: Int
    ): List<Product> {
        return useCase.filterProducts(list, filter, sessionCount)
    }

    fun calcularPrecioConDescuento(productoId: Int, precioOriginal: Double, cupones: List<Coupon>)
            : Double {
        return useCase.calcularPrecioConDescuento(productoId, precioOriginal, cupones)
    }
}