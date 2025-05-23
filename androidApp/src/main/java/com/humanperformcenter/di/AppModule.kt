package com.humanperformcenter.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.AuthUseCase

object AppModule {
    val authUseCase by lazy { AuthUseCase(AuthRepositoryImpl) }
}