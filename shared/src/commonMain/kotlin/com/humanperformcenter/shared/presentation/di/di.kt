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
import com.humanperformcenter.shared.domain.usecase.UserAccountUseCase
import com.humanperformcenter.shared.domain.usecase.AuthUseCase
import com.humanperformcenter.shared.domain.usecase.UserBookingsUseCase
import com.humanperformcenter.shared.domain.usecase.UserCoachesUseCase
import com.humanperformcenter.shared.domain.usecase.UserCouponUseCase
import com.humanperformcenter.shared.domain.usecase.DaySessionUseCase
import com.humanperformcenter.shared.domain.usecase.UserProfileUseCase
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import com.humanperformcenter.shared.domain.usecase.StripeUseCase
import com.humanperformcenter.shared.domain.usecase.UserDocumentUseCase
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

val userProfileModule = module {
    single<UserProfileRepository> { UserProfileRepositoryImpl }

    singleOf(::UserProfileUseCase)

    viewModelOf(::UserProfileViewModel)
}

val userSessionModule = module {
    single<UserAccountRepository> { UserAccountRepositoryImpl }
    single<SessionStorage> { SessionStorageImpl }

    singleOf(::UserAccountUseCase)

    viewModelOf(::UserSessionViewModel)
}

val userFavoritesModule = module {
    single<UserFavoritesRepository> { UserFavoritesRepositoryImpl }

    singleOf(::UserCoachesUseCase)

    viewModelOf(::UserFavoritesViewModel)
}

val userBookingsModule = module {
    single<UserBookingsRepository> { UserBookingsRepositoryImpl }

    singleOf(::UserBookingsUseCase)

    viewModelOf(::UserBookingsViewModel)
}

val userCouponsModule = module {
    single<UserCouponsRepository> { UserCouponsRepositoryImpl }

    singleOf(::UserCouponUseCase)

    viewModelOf(::UserCouponsViewModel)
}

val userDocumentsModule = module {
    single<UserDocumentsRepository> { UserDocumentsRepositoryImpl }

    singleOf(::UserDocumentUseCase)

    viewModelOf(::UserDocumentsViewModel)
    viewModelOf(::UserDocumentSelectionViewModel)
}

val userWalletModule = module {
    single<UserWalletRepository> { UserWalletRepositoryImpl }
    single<UserStatsRepository> { UserStatsRepositoryImpl }

    singleOf(::WalletUseCase)
    singleOf(::UserStatsUseCase)

    viewModelOf(::UserWalletViewModel)
    viewModelOf(::UserStatsViewModel)
}

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl }

    singleOf(::AuthUseCase)

    viewModelOf(::AuthViewModel)
}

val stripeModule = module {
    single<StripeRepository> { StripeRepositoryImpl }

    singleOf(::StripeUseCase)

    viewModelOf(::StripeViewModel)
}

val schedulingModule = module {
    single<DaySessionRepository> { DaySessionRepositoryImpl }

    singleOf(::DaySessionUseCase)

    viewModelOf(::DaySessionViewModel)
}

val catalogModule = module {
    single<ServiceProductRepository> { ServiceProductRepositoryImpl }

    singleOf(::ServiceProductUseCase)

    viewModelOf(::ServiceProductViewModel)
}

val appModule = module {
    includes(
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
