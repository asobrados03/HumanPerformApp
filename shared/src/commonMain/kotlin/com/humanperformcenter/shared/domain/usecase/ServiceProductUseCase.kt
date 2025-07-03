package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.ServicioItembien
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ServiceProductUseCase(private val serviceProductRepository: ServiceProductRepository) {
    suspend fun getAllServices(): List<ServicioDispo> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getAllServices()
    }
    suspend fun getServiceProducts(serviceId: Int): List<ServicioItembien> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getServiceProducts(serviceId)
    }
    suspend fun getUserProducts(customerId: Int): List<ServicioItembien> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getUserProducts(customerId)
    }
}