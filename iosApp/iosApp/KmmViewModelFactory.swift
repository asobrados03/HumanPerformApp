import shared

func makeAuthViewModel() -> shared.AuthViewModel {
    shared.AuthViewModel(authUseCase: AuthUseCase(authRepository: AuthRepositoryImpl(), sessionStorage: SessionStorageImpl()))
}

func makeDaySessionViewModel() -> shared.DaySessionViewModel {
    shared.DaySessionViewModel(useCase: DaySessionUseCase(repository: DaySessionRepositoryImpl()))
}

func makeServiceProductViewModel() -> shared.ServiceProductViewModel {
    shared.ServiceProductViewModel(
        serviceProductUseCase: ServiceProductUseCase(repository: ServiceProductRepositoryImpl()),
        couponUseCase: CouponUseCase(userCouponsRepository: UserCouponsRepositoryImpl())
    )
}

func makeUserSessionViewModel() -> shared.UserSessionViewModel {
    shared.UserSessionViewModel(
        accountUseCase: AccountUseCase(userAccountRepository: UserAccountRepositoryImpl()),
        authUseCase: AuthUseCase(
            authRepository: AuthRepositoryImpl(),
            sessionStorage: SessionStorageImpl()
        )
    )
}

func makeUserProfileViewModel() -> shared.UserProfileViewModel {
    shared.UserProfileViewModel(profileUseCase: ProfileUseCase(userProfileRepository: UserProfileRepositoryImpl()))
}

func makeUserFavoritesViewModel() -> shared.UserFavoritesViewModel {
    shared.UserFavoritesViewModel(coachesUseCase: CoachesUseCase(userFavoritesRepository: UserFavoritesRepositoryImpl()))
}

func makeUserCouponsViewModel() -> shared.UserCouponsViewModel {
    shared.UserCouponsViewModel(couponUseCase: CouponUseCase(userCouponsRepository: UserCouponsRepositoryImpl()))
}

func makeUserDocumentsViewModel() -> shared.UserDocumentsViewModel {
    shared.UserDocumentsViewModel(userDocumentUseCase: UserDocumentUseCase(userDocumentsRepository: UserDocumentsRepositoryImpl()))
}

func makeUserBookingsViewModel() -> shared.UserBookingsViewModel {
    shared.UserBookingsViewModel(
        bookingsUseCase: BookingsUseCase(userBookingsRepository: UserBookingsRepositoryImpl()),
        notificationManager: IOSSessionNotificationManager()
    )
}

func makeUserWalletViewModel() -> shared.UserWalletViewModel {
    shared.UserWalletViewModel(walletUseCase: WalletUseCase(userWalletRepository: UserWalletRepositoryImpl()))
}

func makeUserStatsViewModel() -> shared.UserStatsViewModel {
    shared.UserStatsViewModel(userStatsUseCase: UserStatsUseCase(userStatsRepository: UserStatsRepositoryImpl()))
}

func makeStripeViewModel() -> shared.StripeViewModel {
    shared.StripeViewModel(stripeUseCase: StripeUseCase(stripeRepository: StripeRepositoryImpl()))
}
