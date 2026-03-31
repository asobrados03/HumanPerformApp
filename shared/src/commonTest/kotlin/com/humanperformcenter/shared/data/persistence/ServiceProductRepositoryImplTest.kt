package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.SimpleService
import com.humanperformcenter.shared.data.remote.ServiceProductRemoteDataSource
import com.humanperformcenter.shared.domain.DomainException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ServiceProductRepositoryImplTest {

    @Test
    fun get_service_products_when_success_propagates_expected_products() = runTest {
        val expected = listOf(Product(id = 3, name = "Pack sesiones", serviceIds = listOf(1, 2), isAvailable = true))
        val repository = ServiceProductRepositoryImpl(
            FakeServiceProductRemoteDataSource(getServiceProductsResult = Result.success(expected)),
        )

        val result = repository.getServiceProducts(serviceId = 1, userId = 9)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun get_all_services_when_remote_error_maps_to_domain_not_found() = runTest {
        val repository = ServiceProductRepositoryImpl(
            FakeServiceProductRemoteDataSource(
                getAllServicesResult = Result.failure(IllegalStateException("HTTP 404 Not Found")),
            ),
        )

        val result = repository.getAllServices()

        assertTrue(result.isFailure)
        assertIs<DomainException.NotFound>(result.exceptionOrNull())
    }

    @Test
    fun get_user_products_when_empty_returns_empty_list() = runTest {
        val repository = ServiceProductRepositoryImpl(
            FakeServiceProductRemoteDataSource(getUserProductsResult = Result.success(emptyList())),
        )

        val result = repository.getUserProducts(userId = 2)

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrNull())
    }

    @Test
    fun assign_product_to_user_when_contract_edge_coupon_null_is_supported() = runTest {
        val repository = ServiceProductRepositoryImpl(
            FakeServiceProductRemoteDataSource(assignProductToUserResult = Result.success(77)),
        )

        val result = repository.assignProductToUser(
            userId = 4,
            productId = 11,
            paymentMethod = "cash",
            couponCode = null,
        )

        assertTrue(result.isSuccess)
        assertEquals(77, result.getOrNull())
    }

    private class FakeServiceProductRemoteDataSource(
        private val getAllServicesResult: Result<List<ServiceAvailable>> = Result.success(emptyList()),
        private val getServiceProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val getUserProductsResult: Result<List<Product>> = Result.success(emptyList()),
        private val assignProductToUserResult: Result<Int> = Result.success(1),
        private val unassignProductFromUserResult: Result<Unit> = Result.success(Unit),
        private val getActiveProductDetailResult: Result<ProductDetailResponse> = Result.success(
            ProductDetailResponse(
                createdAt = "2026-03-01",
                name = "Plan",
                services = listOf(SimpleService(id = 1, name = "Training")),
            ),
        ),
        private val getProductDetailHireProductResult: Result<Product> = Result.success(Product(id = 1, name = "Plan")),
    ) : ServiceProductRemoteDataSource {
        override suspend fun getAllServices(): Result<List<ServiceAvailable>> = getAllServicesResult

        override suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> = getServiceProductsResult

        override suspend fun getUserProducts(userId: Int): Result<List<Product>> = getUserProductsResult

        override suspend fun assignProductToUser(
            userId: Int,
            productId: Int,
            paymentMethod: String,
            couponCode: String?,
        ): Result<Int> = assignProductToUserResult

        override suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> = unassignProductFromUserResult

        override suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> =
            getActiveProductDetailResult

        override suspend fun getProductDetailHireProduct(productId: Int): Result<Product> = getProductDetailHireProductResult
    }
}
