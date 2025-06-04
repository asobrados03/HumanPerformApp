package com.humanperformcenter.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserRepositoryImpl
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.UserUseCase

object AppModule {
    val authUseCase by lazy { AuthUseCase(AuthRepositoryImpl) }
    val userUseCase by lazy { UserUseCase(UserRepositoryImpl)}
}