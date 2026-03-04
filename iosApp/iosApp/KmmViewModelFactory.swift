import shared

func makeAuthViewModel() -> shared.AuthViewModel {
    shared.AuthViewModel(authUseCase: AuthUseCase(authRepository: AuthRepositoryImpl()))
}

func makeDaySessionViewModel() -> shared.DaySessionViewModel {
    shared.DaySessionViewModel(useCase: DaySessionUseCase(repository: DaySessionRepositoryImpl()))
}

func makeServiceProductViewModel() -> shared.ServiceProductViewModel {
    shared.ServiceProductViewModel(
        serviceProductUseCase: ServiceProductUseCase(repository: ServiceProductRepositoryImpl()),
        userUseCase: UserUseCase(userRepository: UserRepositoryImpl())
    )
}

func makeUserViewModel() -> shared.UserViewModel {
    shared.UserViewModel(
        userUseCase: UserUseCase(userRepository: UserRepositoryImpl()),
        notificationManager: SessionNotificationManager()
    )
}

func makeUserStatsViewModel() -> shared.UserStatsViewModel {
    shared.UserStatsViewModel(userUseCase: UserUseCase(userRepository: UserRepositoryImpl()))
}

func makeStripeViewModel() -> shared.StripeViewModel {
    shared.StripeViewModel(stripeUseCase: StripeUseCase(stripeRepository: StripeRepositoryImpl()))
}
