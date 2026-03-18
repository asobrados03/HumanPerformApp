package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.data.persistence.AuthRepositoryImpl
import com.humanperformcenter.shared.data.persistence.DaySessionRepositoryImpl
import com.humanperformcenter.shared.data.persistence.ServiceProductRepositoryImpl
import com.humanperformcenter.shared.data.persistence.SessionStorageImpl
import com.humanperformcenter.shared.data.persistence.StripeRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserAccountRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserBookingsRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserCouponsRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserDocumentsRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserFavoritesRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserProfileRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserStatsRepositoryImpl
import com.humanperformcenter.shared.data.persistence.UserWalletRepositoryImpl
import com.humanperformcenter.shared.domain.repository.AuthRepository
import com.humanperformcenter.shared.domain.repository.DaySessionRepository
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.domain.repository.StripeRepository
import com.humanperformcenter.shared.domain.repository.UserAccountRepository
import com.humanperformcenter.shared.domain.repository.UserBookingsRepository
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import com.humanperformcenter.shared.domain.repository.UserDocumentsRepository
import com.humanperformcenter.shared.domain.repository.UserFavoritesRepository
import com.humanperformcenter.shared.domain.repository.UserProfileRepository
import com.humanperformcenter.shared.domain.repository.UserStatsRepository
import com.humanperformcenter.shared.domain.repository.UserWalletRepository
import com.humanperformcenter.shared.domain.storage.SessionStorage
import com.humanperformcenter.shared.domain.usecase.AccountUseCase
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.BookingsUseCase
import com.humanperformcenter.shared.domain.usecase.CoachesUseCase
import com.humanperformcenter.shared.domain.usecase.CouponUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.domain.usecase.ProfileUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.domain.usecase.UserStatsUseCase
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserBookingsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserFavoritesViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    single<AuthRepository> { AuthRepositoryImpl }
    single<SessionStorage> { SessionStorageImpl }
    single<UserProfileRepository> { UserProfileRepositoryImpl }
    single<UserAccountRepository> { UserAccountRepositoryImpl }
    single<UserFavoritesRepository> { UserFavoritesRepositoryImpl }
    single<UserBookingsRepository> { UserBookingsRepositoryImpl }
    single<UserStatsRepository> { UserStatsRepositoryImpl }
    single<UserCouponsRepository> { UserCouponsRepositoryImpl }
    single<UserDocumentsRepository> { UserDocumentsRepositoryImpl }
    single<UserWalletRepository> { UserWalletRepositoryImpl }
    single<DaySessionRepository> { DaySessionRepositoryImpl }
    single<ServiceProductRepository> { ServiceProductRepositoryImpl }
    single<StripeRepository> { StripeRepositoryImpl }

    // UseCases (Mucho más limpio con singleOf)
    singleOf(::AuthUseCase)
    singleOf(::ProfileUseCase)
    singleOf(::AccountUseCase)
    singleOf(::CoachesUseCase)
    singleOf(::BookingsUseCase)
    singleOf(::CouponUseCase)
    singleOf(::WalletUseCase)
    singleOf(::UserStatsUseCase)
    singleOf(::UserDocumentUseCase)
    singleOf(::DaySessionUseCase)
    singleOf(::ServiceProductUseCase)
    singleOf(::StripeUseCase)

    // ViewModels (Ya los tenías bien, pero los agrupamos)
    viewModelOf(::AuthViewModel)
    viewModelOf(::DaySessionViewModel)
    viewModelOf(::ServiceProductViewModel)
    viewModelOf(::UserStatsViewModel)
    viewModelOf(::UserDocumentsViewModel)
    viewModelOf(::UserBookingsViewModel)
    viewModelOf(::UserWalletViewModel)
    viewModelOf(::UserFavoritesViewModel)
    viewModelOf(::UserProfileViewModel)
    viewModelOf(::UserSessionViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::StripeViewModel)
}