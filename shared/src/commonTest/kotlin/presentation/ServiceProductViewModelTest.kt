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
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceProductViewModelTest {

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

    @Test
    fun loadServiceProducts_userProducts_productDetail_and_activeDetail_updateStates() = runTest {
        val product = sampleProduct()
        val detail = sampleProductDetail()
        val vm = ServiceProductViewModel(
            ServiceProductUseCase(
                FakeServiceProductRepository(
                    serviceProductsResult = Result.success(listOf(product)),
                    userProductsResult = Result.success(listOf(product)),
                    productDetailResult = Result.success(product),
                    activeDetailResult = Result.success(detail)
                )
            ),
            UserCouponUseCase(FakeCouponsRepository(Result.success(emptyList())))
        )

        vm.loadServiceProducts(serviceId = 1, userId = 2)
        assertEquals(ServiceProductUiState.Success(listOf(product)), vm.serviceProducts.value[1])

        vm.loadUserProducts(2)
        assertEquals(UserProductsUiState.Success(listOf(product)), vm.userProductsState.value)

        vm.loadProductDetail(1)
        assertEquals(ProductDetailUiState.Success(product), vm.productDetailState.value)

        vm.fetchActiveProductDetail(2, 1)
        assertEquals(ActiveProductDetailState.Success(detail), vm.activeProductDetails.value)
    }

    @Test
    fun loadAllServices_assign_unassign_and_coupons_work() = runTest {
        val service = ServiceAvailable(id = 1, name = "PT")
        val product = sampleProduct(id = 1)
        val coupon = Coupon(1, "PROMO", 10.0, true, LocalDate.parse("2026-12-31"), listOf(1))
        val vm = ServiceProductViewModel(
            ServiceProductUseCase(
                FakeServiceProductRepository(
                    allServicesResult = Result.success(listOf(service)),
                    userProductsResult = Result.success(listOf(product)),
                    assignResult = Result.success(1),
                    unassignResult = Result.success(Unit)
                )
            ),
            UserCouponUseCase(FakeCouponsRepository(Result.success(listOf(coupon))))
        )

        vm.loadAllServices(userId = 7)
        assertEquals(ServiceUiState.Success(listOf(com.humanperformcenter.shared.presentation.ui.models.ServiceUiModel(service, true))), vm.serviceUiState.value)

        vm.assignEvent.test {
            vm.assignProductToUser(7, 1, "card", "PROMO")
            assertEquals(AssignEvent.Success(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.unassignEvent.test {
            vm.unassignProductFromUser(1, 7)
            assertEquals(UnassignEvent.Success, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.loadUserCoupons(7)
        assertEquals(listOf(coupon), vm.userCoupons.value)
    }

    @Test
    fun errorBranches_and_helperFunctions_areCovered() = runTest {
        val vm = ServiceProductViewModel(
            ServiceProductUseCase(
                FakeServiceProductRepository(
                    serviceProductsResult = Result.failure(IllegalStateException("svc error")),
                    userProductsResult = Result.failure(IllegalStateException("user error")),
                    productDetailResult = Result.failure(IllegalStateException("detail error")),
                    activeDetailResult = Result.failure(IllegalStateException()),
                    allServicesResult = Result.failure(IllegalStateException("services error")),
                    assignResult = Result.failure(IllegalStateException("assign error")),
                    unassignResult = Result.failure(IllegalStateException())
                )
            ),
            UserCouponUseCase(FakeCouponsRepository(Result.failure(IllegalStateException("ignore"))))
        )

        vm.loadServiceProducts(3, 4)
        assertEquals(ServiceProductUiState.Error("svc error"), vm.serviceProducts.value[3])

        vm.loadUserProducts(4)
        assertEquals(UserProductsUiState.Error("user error"), vm.userProductsState.value)

        vm.loadProductDetail(9)
        assertEquals(ProductDetailUiState.Error("detail error"), vm.productDetailState.value)

        vm.fetchActiveProductDetail(4, 9)
        assertEquals(ActiveProductDetailState.Error("Error al cargar el producto"), vm.activeProductDetails.value)

        vm.loadAllServices(4)
        assertEquals(ServiceUiState.Error("services error"), vm.serviceUiState.value)

        vm.assignEvent.test {
            vm.assignProductToUser(7, 1, "card")
            assertEquals(AssignEvent.Error("assign error"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.unassignEvent.test {
            vm.unassignProductFromUser(1, 7)
            assertEquals(UnassignEvent.Error("No se pudo eliminar"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val list = listOf(
            sampleProduct(id = 1, type = "recurrent", sessions = 12),
            sampleProduct(id = 2, type = "single", sessions = 1)
        )
        assertEquals(1, vm.filterProducts(list, ProductTypeFilter.RECURRENT, 0).size)
        assertEquals(2, vm.filterProducts(list, ProductTypeFilter.ALL, 0).size)
        assertEquals(90.0, vm.calculateDiscountedPrice(1, 100.0, listOf(
            Coupon(1, "P10", 10.0, true, LocalDate.parse("2026-12-31"), listOf(1))
        )))
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
