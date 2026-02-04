package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.data.persistence.ServiceProductRepositoryImpl
import com.humanperformcenter.shared.data.persistence.StripeRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserRepositoryImpl
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.domain.repository.StripeRepository
import com.humanperformcenter.shared.domain.repository.UserRepository
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.domain.usecase.UserUseCase
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    single<AuthRepository> { AuthRepositoryImpl }
    single<UserRepository> { UserRepositoryImpl }
    single<DaySessionRepository> { DaySessionRepositoryImpl }
    single<ServiceProductRepository> { ServiceProductRepositoryImpl }
    single<StripeRepository> { StripeRepositoryImpl }

    // UseCases (Mucho más limpio con singleOf)
    singleOf(::AuthUseCase)
    singleOf(::UserUseCase)
    singleOf(::DaySessionUseCase)
    singleOf(::ServiceProductUseCase)
    singleOf(::StripeUseCase)

    // ViewModels (Ya los tenías bien, pero los agrupamos)
    viewModelOf(::AuthViewModel)
    viewModelOf(::DaySessionViewModel)
    viewModelOf(::ServiceProductViewModel)
    viewModelOf(::UserStatsViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::StripeViewModel)
}