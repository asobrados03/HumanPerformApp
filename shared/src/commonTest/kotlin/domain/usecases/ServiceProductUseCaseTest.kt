package com.humanperformcenter.shared.domain.usecases

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceProductUseCaseTest {
    private class FakeServiceProductRepository : ServiceProductRepository {
        override suspend fun getAllServices() = Result.success(emptyList<com.humanperformcenter.shared.data.model.product_service.ServiceAvailable>())
        override suspend fun getServiceProducts(serviceId: Int, userId: Int) = Result.success(emptyList<Product>())
        override suspend fun getUserProducts(userId: Int) = Result.success(emptyList<Product>())
        override suspend fun assignProductToUser(userId: Int, productId: Int, paymentMethod: String, couponCode: String?) = Result.success(1)
        override suspend fun unassignProductFromUser(userId: Int, productId: Int) = Result.success(Unit)
        override suspend fun getActiveProductDetail(userId: Int, productId: Int) = error("Not used")
        override suspend fun getProductDetailHireProduct(productId: Int) = error("Not used")
    }

    private val useCase = ServiceProductUseCase(FakeServiceProductRepository())

    @Test
    fun serviceProductUseCase_whenFilterMatches_returnsProducts() {
        // Arrange
        val products = listOf(Product(id = 1, name = "Mensual", typeOfProduct = "single_payment", session = 8))

        // Act
        val result = useCase.filterProducts(products, ProductTypeFilter.SinglePayment, 8)

        // Assert
        assertEquals(1, result.size)
    }

    @Test
    fun serviceProductUseCase_whenNoProducts_returnsEmptyList() {
        // Arrange
        val products = emptyList<Product>()

        // Act
        val result = useCase.filterProducts(products, ProductTypeFilter.All, 0)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun serviceProductUseCase_whenCouponDiscountExceedsPrice_returnsZero() = runBlocking {
        // Arrange
        val coupons = listOf(
            Coupon(1, "FREE", 200.0, isPercentage = false, expiryDate = LocalDate(2026, 12, 1), productIds = listOf(1))
        )

        // Act
        val result = useCase.calculateDiscountedPrice(productId = 1, originalPrice = 50.0, coupons = coupons)

        // Assert
        assertEquals(0.0, result)
    }
}
