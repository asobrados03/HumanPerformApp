package com.humanperformcenter.shared.presentation

import app.cash.turbine.test
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.SimpleService
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
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
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServiceProductViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeServiceProductRepository(
        private val allServicesResult: Result<List<ServiceAvailable>> = Result.success(emptyList()),
        private val serviceProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val userProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val assignResult: Result<Int> = Result.success(1),
        private val unassignResult: Result<Unit> = Result.success(Unit),
        private val activeDetailResult: Result<ProductDetailResponse> = Result.success(sampleProductDetail()),
        private val productDetailResult: Result<Product> = Result.success(sampleProduct())
    ) : ServiceProductRepository {
        override suspend fun getAllServices(): Result<List<ServiceAvailable>> = allServicesResult
        override suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> = serviceProductsResult
        override suspend fun getUserProducts(userId: Int): Result<List<Product>> = userProductsResult
        override suspend fun assignProductToUser(userId: Int, productId: Int, paymentMethod: String, couponCode: String?): Result<Int> = assignResult
        override suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> = unassignResult
        override suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> = activeDetailResult
        override suspend fun getProductDetailHireProduct(productId: Int): Result<Product> = productDetailResult
    }

    private class FakeCouponsRepository(
        private val couponsResult: Result<List<Coupon>>
    ) : UserCouponsRepository {
        override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> = Result.success(Unit)
        override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = couponsResult
    }

    private fun buildViewModel(
        serviceRepository: FakeServiceProductRepository = FakeServiceProductRepository(),
        couponsResult: Result<List<Coupon>> = Result.success(emptyList())
    ) = ServiceProductViewModel(
        ServiceProductUseCase(serviceRepository),
        UserCouponUseCase(FakeCouponsRepository(couponsResult))
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadServiceProducts_loadUserProducts_loadProductDetail_and_fetchActiveProductDetail_when_success_update_states() = runTest {
        // Arrange
        val product = sampleProduct()
        val detail = sampleProductDetail()
        val viewModel = buildViewModel(
        // Act
            serviceRepository = FakeServiceProductRepository(
                serviceProductsResult = Result.success(listOf(product)),
                userProductsResult = Result.success(listOf(product)),
                productDetailResult = Result.success(product),
                activeDetailResult = Result.success(detail)
            )
        )

        viewModel.loadServiceProducts(serviceId = 1, userId = 2)
        viewModel.loadUserProducts(2)
        viewModel.loadProductDetail(1)
        viewModel.fetchActiveProductDetail(2, 1)
        advanceUntilIdle()

        // Assert
        assertEquals(ServiceProductUiState.Success(listOf(product)), viewModel.serviceProducts.value[1])
        assertEquals(UserProductsUiState.Success(listOf(product)), viewModel.userProductsState.value)
        assertEquals(ProductDetailUiState.Success(product), viewModel.productDetailState.value)
        assertEquals(ActiveProductDetailState.Success(detail), viewModel.activeProductDetails.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadAllServices_when_success_marks_owned_services() = runTest {
        // Arrange
        val service = ServiceAvailable(id = 1, name = "PT")
        val product = sampleProduct(id = 1)
        val viewModel = buildViewModel(
        // Act
            serviceRepository = FakeServiceProductRepository(
                allServicesResult = Result.success(listOf(service)),
                userProductsResult = Result.success(listOf(product))
            )
        )

        viewModel.loadAllServices(userId = 7)
        advanceUntilIdle()

        // Assert
        assertEquals(
            ServiceUiState.Success(listOf(ServiceUiModel(service, true))),
            viewModel.serviceUiState.value
        )
    }

    @Test
    fun assignProductToUser_and_unassignProductFromUser_when_success_emit_success_events() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            serviceRepository = FakeServiceProductRepository(
                assignResult = Result.success(1),
                unassignResult = Result.success(Unit)
            )
        )

        viewModel.assignEvent.test {
            viewModel.assignProductToUser(7, 1, "card", "PROMO")
        // Assert
            assertEquals(AssignEvent.Success(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.unassignEvent.test {
            viewModel.unassignProductFromUser(1, 7)
            assertEquals(UnassignEvent.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loadUserCoupons_when_success_updates_coupons_list() = runTest {
        // Arrange
        val coupon = Coupon(1, "PROMO", 10.0, true, LocalDate.parse("2026-12-31"), listOf(1))
        val viewModel = buildViewModel(couponsResult = Result.success(listOf(coupon)))

        // Act
        viewModel.loadUserCoupons(7)
        advanceUntilIdle()

        // Assert
        assertEquals(listOf(coupon), viewModel.userCoupons.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun error_branches_emit_expected_errors() = runTest {
        // Arrange
        val viewModel = buildViewModel(
        // Act
            serviceRepository = FakeServiceProductRepository(
                serviceProductsResult = Result.failure(IllegalStateException("svc error")),
                userProductsResult = Result.failure(IllegalStateException("user error")),
                productDetailResult = Result.failure(IllegalStateException("detail error")),
                activeDetailResult = Result.failure(IllegalStateException()),
                allServicesResult = Result.failure(IllegalStateException("services error")),
                assignResult = Result.failure(IllegalStateException("assign error")),
                unassignResult = Result.failure(IllegalStateException())
            )
        )

        viewModel.loadServiceProducts(3, 4)
        viewModel.loadUserProducts(4)
        viewModel.loadProductDetail(9)
        viewModel.fetchActiveProductDetail(4, 9)
        viewModel.loadAllServices(4)
        advanceUntilIdle()

        // Assert
        assertEquals(ServiceProductUiState.Error("svc error"), viewModel.serviceProducts.value[3])
        assertEquals(UserProductsUiState.Error("user error"), viewModel.userProductsState.value)
        assertEquals(ProductDetailUiState.Error("detail error"), viewModel.productDetailState.value)
        assertEquals(ActiveProductDetailState.Error("Error al cargar el producto"), viewModel.activeProductDetails.value)
        assertEquals(ServiceUiState.Error("services error"), viewModel.serviceUiState.value)

        viewModel.assignEvent.test {
            viewModel.assignProductToUser(7, 1, "card")
            assertEquals(AssignEvent.Error("assign error"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.unassignEvent.test {
            viewModel.unassignProductFromUser(1, 7)
            assertEquals(UnassignEvent.Error("No se pudo eliminar"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun helper_functions_filterProducts_and_calculatediscountedprice_return_expected_values() {
        // Arrange
        val viewModel = buildViewModel()
        val products = listOf(
        // Act
            sampleProduct(id = 1, type = "recurrent", sessions = 12),
            sampleProduct(id = 2, type = "single", sessions = 1)
        )

        // Assert
        assertEquals(1, viewModel.filterProducts(products, ProductTypeFilter.RECURRENT, 0).size)
        assertEquals(2, viewModel.filterProducts(products, ProductTypeFilter.ALL, 0).size)
        assertEquals(
            90.0,
            viewModel.calculateDiscountedPrice(
                1,
                100.0,
                listOf(Coupon(1, "P10", 10.0, true, LocalDate.parse("2026-12-31"), listOf(1)))
            )
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun service_state_helpers_return_expected_values() = runTest {
        val service = ServiceAvailable(id = 1, name = "PT")
        val viewModel = buildViewModel(
            serviceRepository = FakeServiceProductRepository(
                allServicesResult = Result.success(listOf(service)),
                userProductsResult = Result.success(emptyList())
            )
        )

        assertEquals("loading", viewModel.serviceStateKind())
        assertEquals(emptyList(), viewModel.serviceStateServices())
        assertNull(viewModel.serviceStateMessage())

        viewModel.loadAllServices(userId = 7)
        advanceUntilIdle()

        assertEquals("success", viewModel.serviceStateKind())
        assertEquals(listOf(ServiceUiModel(service, false)), viewModel.serviceStateServices())
        assertNull(viewModel.serviceStateMessage())
    }

    private companion object {
        fun sampleProduct(id: Int = 1, type: String = "recurrent", sessions: Int = 12) = Product(
            id = id,
            name = "Producto $id",
            description = "desc",
            price = 100.0,
            image = null,
            typeOfProduct = type,
            priceId = "price_$id",
            session = sessions,
            serviceIds = listOf(1),
            isAvailable = true,
            stripeSubscriptionId = null,
            stripePaymentIntentId = null
        )

        fun sampleProductDetail() = ProductDetailResponse(
            createdAt = "2026-03-27",
            expiryDate = "2026-06-27",
            amount = 100.0,
            discount = 10.0,
            totalAmount = 90.0,
            paymentMethod = "card",
            paymentStatus = "paid",
            name = "Pack",
            image = null,
            description = "desc",
            services = listOf(SimpleService(1, "PT"))
        )
    }
}
