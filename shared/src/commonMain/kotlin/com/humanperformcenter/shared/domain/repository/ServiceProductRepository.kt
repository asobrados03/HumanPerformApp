package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.ServicioDispo
import com.humanperformcenter.shared.data.model.ServicioItembien

interface ServiceProductRepository {
    suspend fun getAllServices(): List<ServicioDispo>
    suspend fun getServiceProducts(serviceId: Int): List<ServicioItembien>
    suspend fun getUserProducts(customerId: Int): List<ServicioItembien>
    suspend fun assignProductToUser(userId: Int, productId: Int): Boolean
    suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean
}