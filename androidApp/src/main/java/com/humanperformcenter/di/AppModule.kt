package com.humanperformcenter.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.persistence.BlogRepositoryImpl
import com.humanperformcenter.shared.data.persistence.ServiceProductRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserRepositoryImpl
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.BlogUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase

object AppModule {
    val authUseCase by lazy { AuthUseCase(AuthRepositoryImpl) }
    val userUseCase by lazy { UserUseCase(UserRepositoryImpl) }
    val blogUseCase by lazy { BlogUseCase(BlogRepositoryImpl) }
    val daySessionUseCase by lazy {DaySessionUseCase (DaySessionRepositoryImpl) }
    val serviceProductUseCase by lazy { ServiceProductUseCase(ServiceProductRepositoryImpl) }
}