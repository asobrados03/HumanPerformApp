package com.humanperformcenter.shared.presentation.di

import com.humanperformcenter.shared.data.local.AuthLocalDataSource
import com.humanperformcenter.shared.data.local.UserProfileLocalDataSource
import com.humanperformcenter.shared.data.local.impl.AuthLocalDataSourceImpl
import com.humanperformcenter.shared.data.local.impl.UserProfileLocalDataSourceImpl
import com.humanperformcenter.shared.data.network.DefaultHttpClientProvider
import com.humanperformcenter.shared.data.network.HttpClientProvider
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
import com.humanperformcenter.shared.data.remote.AuthRemoteDataSource
import com.humanperformcenter.shared.data.remote.DaySessionRemoteDataSource
import com.humanperformcenter.shared.data.remote.ServiceProductRemoteDataSource
import com.humanperformcenter.shared.data.remote.StripeRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserAccountRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserBookingsRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserCouponsRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserDocumentsRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserFavoritesRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserProfileRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserStatsRemoteDataSource
import com.humanperformcenter.shared.data.remote.UserWalletRemoteDataSource
import com.humanperformcenter.shared.data.remote.impl.AuthRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.DaySessionRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.ServiceProductRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.StripeRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserAccountRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserBookingsRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserCouponsRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserDocumentsRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserFavoritesRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserProfileRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserStatsRemoteDataSourceImpl
import com.humanperformcenter.shared.data.remote.impl.UserWalletRemoteDataSourceImpl
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
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import com.humanperformcenter.shared.domain.usecase.UserBookingsUseCase
import com.humanperformcenter.shared.domain.usecase.UserCoachesUseCase
import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import com.humanperformcenter.shared.domain.usecase.UserStatsUseCase
import com.humanperformcenter.shared.domain.usecase.WalletUseCase
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserBookingsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserCouponsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentSelectionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserFavoritesViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserProfileViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val localStorageModule = module {
    single<AuthLocalDataSource> { AuthLocalDataSourceImpl }
    single<UserProfileLocalDataSource> { UserProfileLocalDataSourceImpl }
}

val networkModule = module {
    single<HttpClientProvider> { DefaultHttpClientProvider }
    single<AuthRemoteDataSource> { AuthRemoteDataSourceImpl(get(), get()) }
    single<DaySessionRemoteDataSource> { DaySessionRemoteDataSourceImpl(get()) }
    single<ServiceProductRemoteDataSource> { ServiceProductRemoteDataSourceImpl(get()) }
    single<StripeRemoteDataSource> { StripeRemoteDataSourceImpl(get()) }
    single<UserAccountRemoteDataSource> { UserAccountRemoteDataSourceImpl(get()) }
    single<UserBookingsRemoteDataSource> { UserBookingsRemoteDataSourceImpl(get()) }
    single<UserCouponsRemoteDataSource> { UserCouponsRemoteDataSourceImpl(get()) }
    single<UserDocumentsRemoteDataSource> { UserDocumentsRemoteDataSourceImpl(get()) }
    single<UserFavoritesRemoteDataSource> { UserFavoritesRemoteDataSourceImpl(get()) }
    single<UserProfileRemoteDataSource> { UserProfileRemoteDataSourceImpl(get()) }
    single<UserStatsRemoteDataSource> { UserStatsRemoteDataSourceImpl(get()) }
    single<UserWalletRemoteDataSource> { UserWalletRemoteDataSourceImpl(get()) }
}

val userProfileModule = module {
    single<UserProfileRepository> { UserProfileRepositoryImpl(get(), get()) }
    singleOf(::UserProfileUseCase)
    viewModelOf(::UserProfileViewModel)
}

val userSessionModule = module {
    single<UserAccountRepository> { UserAccountRepositoryImpl(get()) }
    single<SessionStorage> { SessionStorageImpl(get()) }
    singleOf(::UserAccountUseCase)
    viewModelOf(::UserSessionViewModel)
}

val userFavoritesModule = module {
    single<UserFavoritesRepository> { UserFavoritesRepositoryImpl(get()) }
    singleOf(::UserCoachesUseCase)
    viewModelOf(::UserFavoritesViewModel)
}

val userBookingsModule = module {
    single<UserBookingsRepository> { UserBookingsRepositoryImpl(get()) }
    singleOf(::UserBookingsUseCase)
    viewModelOf(::UserBookingsViewModel)
}

val userCouponsModule = module {
    single<UserCouponsRepository> { UserCouponsRepositoryImpl(get()) }
    singleOf(::UserCouponUseCase)
    viewModelOf(::UserCouponsViewModel)
}

val userDocumentsModule = module {
    single<UserDocumentsRepository> { UserDocumentsRepositoryImpl(get()) }
    singleOf(::UserDocumentUseCase)
    viewModelOf(::UserDocumentsViewModel)
    viewModelOf(::UserDocumentSelectionViewModel)
}

val userWalletModule = module {
    single<UserWalletRepository> { UserWalletRepositoryImpl(get()) }
    single<UserStatsRepository> { UserStatsRepositoryImpl(get()) }
    singleOf(::WalletUseCase)
    singleOf(::UserStatsUseCase)
    viewModelOf(::UserWalletViewModel)
    viewModelOf(::UserStatsViewModel)
}

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    singleOf(::AuthUseCase)
    viewModelOf(::AuthViewModel)
}

val stripeModule = module {
    single<StripeRepository> { StripeRepositoryImpl(get()) }
    singleOf(::StripeUseCase)
    viewModelOf(::StripeViewModel)
}

val schedulingModule = module {
    single<DaySessionRepository> { DaySessionRepositoryImpl(get()) }
    singleOf(::DaySessionUseCase)
    viewModelOf(::DaySessionViewModel)
}

val catalogModule = module {
    single<ServiceProductRepository> { ServiceProductRepositoryImpl(get()) }
    singleOf(::ServiceProductUseCase)
    viewModelOf(::ServiceProductViewModel)
}

val appModule = module {
    includes(
        localStorageModule,
        networkModule,
        userProfileModule,
        userSessionModule,
        userFavoritesModule,
        userBookingsModule,
        userCouponsModule,
        userDocumentsModule,
        userWalletModule,
        authModule,
        stripeModule,
        schedulingModule,
        catalogModule,
    )
}
