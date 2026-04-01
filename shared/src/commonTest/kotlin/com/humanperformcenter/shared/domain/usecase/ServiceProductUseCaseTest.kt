package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.SimpleService
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatform.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceProductUseCaseTest : KoinTest {

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun getAllServices_whenServicesExist_returnsList() = runTest {
        val expected = listOf(ServiceAvailable(1, "Fisioterapia", "img.png"))
        val useCase = buildUseCase(FakeRepo(getAllResult = Result.success(expected)))
        assertEquals(expected, useCase.getAllServices().getOrNull())
    }

    @Test
    fun getServiceProducts_whenServiceIsValid_returnsProducts() = runTest {
        val expected = listOf(Product(1, "Plan mensual"))
        val useCase = buildUseCase(FakeRepo(getServiceProductsResult = Result.success(expected)))
        assertEquals(expected, useCase.getServiceProducts(1, 9).getOrNull())
    }

    @Test
    fun getUserProducts_whenRepositoryFails_propagatesFailure() = runTest {
        val useCase = buildUseCase(FakeRepo(getUserProductsResult = Result.failure(RuntimeException("backend error"))))
        assertTrue(useCase.getUserProducts(99).isFailure)
    }

    @Test
    fun assignProductToUser_whenDataIsValid_returnsAssignmentId() = runTest {
        val useCase = buildUseCase(FakeRepo(assignResult = Result.success(321)))
        assertEquals(321, useCase.assignProductToUser(1, 2, "card", "WELCOME").getOrNull())
    }

    @Test
    fun unassignProductFromUser_whenDataIsValid_returnsSuccess() = runTest {
        val useCase = buildUseCase(FakeRepo(unassignResult = Result.success(Unit)))
        assertTrue(useCase.unassignProductFromUser(1, 2).isSuccess)
    }

    @Test
    fun getActiveProductDetail_whenProductExists_returnsDetail() = runTest {
        val detail = ProductDetailResponse("2026-01-01", name = "Plan", services = listOf(SimpleService(1, "S")))
        val useCase = buildUseCase(FakeRepo(activeDetailResult = Result.success(detail)))
        assertEquals("Plan", useCase.getActiveProductDetail(1, 2).getOrNull()?.name)
    }

    @Test
    fun getProductDetailHireProduct_whenProductExists_returnsProduct() = runTest {
        val useCase = buildUseCase(FakeRepo(hireProductResult = Result.success(Product(1, "Plan"))))
        assertEquals("Plan", useCase.getProductDetailHireProduct(1).getOrNull()?.name)
    }

    @Test
    fun filterProducts_whenFilterIsRecurrent_returnsOnlyRecurring() {
        val useCase = buildUseCase(FakeRepo())
        val products = listOf(
            Product(1, "Plan mensual", typeOfProduct = "recurrent", session = 4),
            Product(2, "Bono 10", typeOfProduct = "single", session = 10)
        )
        val result = useCase.filterProducts(products, ProductTypeFilter.RECURRENT, 0)
        assertEquals(listOf(products[0]), result)
    }

    @Test
    fun calculateDiscountedPrice_whenDiscountExceedsPrice_returnsZero() {
        val useCase = buildUseCase(FakeRepo())
        val coupons = listOf(Coupon(1, "BIG", 500.0, false, LocalDate.parse("2026-12-31"), listOf(3)))
        assertEquals(0.0, useCase.calculateDiscountedPrice(3, 120.0, coupons))
    }

    private fun buildUseCase(repo: ServiceProductRepository): ServiceProductUseCase {
        startKoin { modules(module { single<ServiceProductRepository> { repo }; single { ServiceProductUseCase(get()) } }) }
        return KoinPlatform.getKoin().get()
    }

    private class FakeRepo(
        private val getAllResult: Result<List<ServiceAvailable>> = Result.success(emptyList()),
        private val getServiceProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val getUserProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val assignResult: Result<Int> = Result.success(1),
        private val unassignResult: Result<Unit> = Result.success(Unit),
        private val activeDetailResult: Result<ProductDetailResponse> = Result.success(
            ProductDetailResponse("2026-01-01", name = "Plan", services = listOf(SimpleService(1, "S")))
        ),
        private val hireProductResult: Result<Product> = Result.success(Product(1, "Plan")),
    ) : ServiceProductRepository {
        override suspend fun getAllServices() = getAllResult
        override suspend fun getServiceProducts(serviceId: Int, userId: Int) = getServiceProductsResult
        override suspend fun getUserProducts(userId: Int) = getUserProductsResult
        override suspend fun assignProductToUser(userId: Int, productId: Int, paymentMethod: String, couponCode: String?) = assignResult
        override suspend fun unassignProductFromUser(userId: Int, productId: Int) = unassignResult
        override suspend fun getActiveProductDetail(userId: Int, productId: Int) = activeDetailResult
        override suspend fun getProductDetailHireProduct(productId: Int) = hireProductResult
    }
}
