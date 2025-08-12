package com.humanperformcenter.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.persistence.ServiceProductRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserRepositoryImpl
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.data.persistence.GooglePayRepository
import com.humanperformcenter.shared.data.persistence.PaymentRepositoryImpl
import com.humanperformcenter.shared.data.persistence.StripeRepositoryImpl
import com.humanperformcenter.shared.domain.repository.StripeRepository
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.domain.usecase.GooglePayUseCase
import com.humanperformcenter.shared.domain.usecase.PaymentUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import kotlin.getValue

object AppModule {
    val authUseCase by lazy { AuthUseCase(AuthRepositoryImpl) }
    val userUseCase by lazy { UserUseCase(UserRepositoryImpl) }
    val googlePayUseCase by lazy { GooglePayUseCase(GooglePayRepository) }
    val paymentUseCase by lazy { PaymentUseCase(PaymentRepositoryImpl) }
    val daySessionUseCase by lazy {DaySessionUseCase (DaySessionRepositoryImpl) }
    val serviceProductUseCase by lazy { ServiceProductUseCase(ServiceProductRepositoryImpl) }
    val stripeUseCase by lazy { StripeUseCase(StripeRepositoryImpl) }
}