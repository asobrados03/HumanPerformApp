package com.humanperformcenter.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.humanperformcenter.app.SetStatusBarColor
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.screens.AddCouponScreen
import com.humanperformcenter.ui.screens.CalendarScreen
import com.humanperformcenter.ui.screens.ChangePasswordScreen
import com.humanperformcenter.ui.screens.DocumentScreen
import com.humanperformcenter.ui.screens.ElectronicWalletScreen
import com.humanperformcenter.ui.screens.EnterEmailScreen
import com.humanperformcenter.ui.screens.HireProductScreen
import com.humanperformcenter.ui.screens.LoginScreen
import com.humanperformcenter.ui.screens.MyProfileScreen
import com.humanperformcenter.ui.screens.PasswordResetInfoScreen
import com.humanperformcenter.ui.screens.PaymentScreen
import com.humanperformcenter.ui.screens.PaymentSuccessScreen
import com.humanperformcenter.ui.screens.ProductDetailScreen
import com.humanperformcenter.ui.screens.RegisterScreen
import com.humanperformcenter.ui.screens.ServicesScreen
import com.humanperformcenter.ui.screens.SplashScreen
import com.humanperformcenter.ui.screens.StripeCheckoutScreen
import com.humanperformcenter.ui.screens.UserScreen
import com.humanperformcenter.ui.screens.UserStatsScreen
import com.humanperformcenter.ui.screens.ViewPaymentMethodScreen
import com.humanperformcenter.ui.screens.WelcomeScreen
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.DaySessionViewModelFactory
import com.humanperformcenter.ui.viewmodel.PaymentViewModel
import com.humanperformcenter.ui.viewmodel.PaymentViewModelFactory
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModelFactory
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserStatsViewModel
import com.humanperformcenter.ui.viewmodel.UserStatsViewModelFactory
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModelFactory
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

@Composable
fun Navigation(
    sessionViewModel: SessionViewModel,
    navController: NavHostController,
    paymentSheet: PaymentSheet,
    registerPaymentSheetResult: ((PaymentSheetResult) -> Unit) -> Unit,
    onPlaySound: (Int) -> Unit
) {
    SetStatusBarColor(
        statusBarColor = Color(0xFFB71C1C),
        navigationBarColor = Color(0xFFB71C1C)
    )

    val isLoggedIn by sessionViewModel
        .isLoggedInFlow
        .collectAsState(initial = false)

    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(AppModule.googlePayUseCase, AppModule.paymentUseCase, AppModule.stripeUseCase)
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(AppModule.userUseCase)
    )

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(AppModule.authUseCase)
    )

    LaunchedEffect(Unit) {
        ApiClient.logoutEvents.collect {
            navController.navigate(Welcome) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    key(isLoggedIn) {
        NavHost(
            navController = navController,
            startDestination = Splash
        ) {
            composable<Splash> {
                SplashScreen(navController, sessionViewModel)
            }
            composable<Welcome> {
                WelcomeScreen(
                    onNavigateToRegister = {
                        navController.navigate(Register)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Login)
                    }
                )
            }
            composable<Register> {
                RegisterScreen(
                    onRegistroExitoso = {
                        navController.navigate(Login) {
                            popUpTo(Register) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Login)
                    },
                    navController = navController
                )
            }
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Service) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Register)
                    },
                    navController = navController
                )
            }
            composable<EnterEmail> {
                val resetPasswordState by authViewModel.isResettingPassword
                    .collectAsStateWithLifecycle()

                EnterEmailScreen(
                    onEmailSubmit = { email ->
                        // Llamada en segundo plano de la petición a la API a traves del authViewModel
                        authViewModel.resetPassword(email)
                    },
                    resetPasswordState = resetPasswordState,
                    onResetState = {
                        authViewModel.resetResettingPasswordState()
                    },
                    navController = navController
                )
            }
            composable<PasswordResetInfo> {
                PasswordResetInfoScreen(navController = navController)
            }
            composable<Service> {
                ServicesScreen(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    userViewModel = viewModel(
                        factory = UserViewModelFactory(AppModule.userUseCase)
                    ),
                    serviceProductViewModel = viewModel(
                        factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase)
                    ),
                    daySessionViewModel = viewModel(
                        factory = DaySessionViewModelFactory(AppModule.daySessionUseCase)
                    )
                )
            }
            composable<HireProduct> { backStackEntry ->
                // Reconstruimos el objeto ServicioRoute
                val route = backStackEntry.toRoute<HireProduct>()
                val userId by sessionViewModel.userId.collectAsStateWithLifecycle()

                // Sólo mostramos si tenemos usuario
                if (userId != null) {
                    HireProductScreen(
                        serviceId     = route.serviceId,
                        navController = navController,
                        viewModel     = viewModel(
                            factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase)
                        ),
                        paymentViewModel = paymentViewModel,
                        sesionViewModel = sessionViewModel
                    )
                }
            }
            composable<User> {
                val loading by userViewModel.isLoading.collectAsStateWithLifecycle()

                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        UserScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            onEditProfile = { navController.navigate(EditProfile) },
                            onViewProfile = { navController.navigate(MyProfile) },
                            onMenuClick = { option ->
                                when (option) {
                                    MenuOption.FAVORITOS -> navController.navigate(FavoriteCoach)
                                    MenuOption.DOCUMENTO -> navController.navigate(Document)
                                    MenuOption.VER_PAGO -> navController.navigate(ViewPaymentMethod)
                                    MenuOption.MONEDERO_VIRTUAL -> navController.navigate(ElectronicWallet)
                                    MenuOption.CONFIGURACION -> navController.navigate(Configuration)
                                    MenuOption.ANADIR_CUPON   -> navController.navigate(AddCoupon)
                                }
                            }
                        )
                    }
                }
            }
            composable<ViewPaymentMethod>{
                val userData by userViewModel.userData.collectAsStateWithLifecycle()

                val userId = userData?.id ?: -1

                ViewPaymentMethodScreen(
                    navController = navController,
                    paymentViewModel = paymentViewModel,
                    userId = userId
                )
            }
            composable<AddCoupon>{
                val userState by userViewModel.userData.collectAsStateWithLifecycle()

                when (val user = userState) {
                    null -> {
                        // Mientras no tenemos usuario, muestra loading
                        FullScreenLoading()
                    }
                    else -> {
                        // Solo cuando user != null, pasamos su id real (Int) a la pantalla
                        AddCouponScreen(
                            navController  = navController,
                            userId         = user.id,
                            userViewModel  = userViewModel
                        )
                    }
                }
            }
            composable<Configuration> {
                ConfigurationRoute(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
            composable<ChangePassword> {
                val changePasswordState by authViewModel.isChangingPassword
                    .collectAsStateWithLifecycle()

                val loading by userViewModel.isLoading.collectAsState()
                val user by userViewModel.userData.collectAsState()

                when {
                    loading -> {
                        // Mostrar spinner mientras isLoading == true
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    user == null -> {
                        LaunchedEffect(Unit) {
                            navController.navigate(Login) {
                                popUpTo(Configuration) { inclusive = true }
                            }
                        }
                    }

                    else -> {
                        ChangePasswordScreen(
                            navController = navController,
                            changePasswordState = changePasswordState,
                            onChangePassword = { current, newPass, confirm, userId ->
                                authViewModel.changePassword(current, newPass, confirm, userId)
                            },
                            onResetState = {
                                authViewModel.resetChangePasswordState()
                            },
                            user = user!!
                        )
                    }
                }
            }
            composable<Document> {
                DocumentScreen(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
            composable<ElectronicWallet> {
                ElectronicWalletScreen(
                    navController = navController,
                    userViewModel = userViewModel,
                    userId = sessionViewModel.userId.collectAsState().value ?: 0
                )
            }
            composable<StartPayment> {
                PaymentScreen(viewModel = paymentViewModel, navController = navController)
            }
            composable<PaymentSuccess> {
                PaymentSuccessScreen(navController = navController)
            }
            composable<StripeCheckout> {
                StripeCheckoutScreen(navController, paymentViewModel,
                    viewModel(factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase)) ,
                    userId = sessionViewModel.userId.collectAsState().value ?: 0 ,paymentSheet,registerPaymentSheetResult)
            }

            composable<EditProfile> {
                EditProfileRoute(navController = navController)
            }
            composable<MyProfile> {
                val loading by userViewModel.isLoading.collectAsState()
                val userState by userViewModel.userData.collectAsState()

                when {
                    loading -> {
                        // Mostrar spinner mientras isLoading == true
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    userState == null -> {
                        LaunchedEffect(Unit) {
                            navController.navigate(Login) {
                                popUpTo(User) { inclusive = true }
                            }
                        }
                    }

                    else -> {
                        MyProfileScreen(
                            user = userState!!,
                            navController = navController
                        )
                    }
                }
            }
            composable<FavoriteCoach> {
                FavoriteRoute(
                    userViewModel = userViewModel,
                    navController = navController
                )
            }

            composable<Calendar> {
                val daySessionViewModel: DaySessionViewModel = viewModel(
                    factory = DaySessionViewModelFactory(AppModule.daySessionUseCase)
                )

                CalendarScreen(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    userViewModel = userViewModel,
                    onPlaySound = onPlaySound,
                    daySessionViewModel = daySessionViewModel
                )
            }
            composable<Stats> {
                val statsViewModel: UserStatsViewModel = viewModel(
                    factory = UserStatsViewModelFactory(AppModule.userUseCase)
                )
                val userId by sessionViewModel.userId.collectAsState()

                LaunchedEffect(userId) {
                    statsViewModel.loadStatistics(userId!!)
                }

                UserStatsScreen(
                    navController = navController,
                    statsViewModel = statsViewModel,
                    onRetry = { statsViewModel.loadStatistics(userId ?: 0) }
                )
            }
            composable<ProductDetail> { backStackEntry ->
                val productDetail = backStackEntry.toRoute<ProductDetail>()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Service)
                }

                val viewModel: ServiceProductViewModel = viewModel(
                    parentEntry,
                    factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase)
                )
                val userId by sessionViewModel.userId.collectAsState()

                if (userId != null) {
                    ProductDetailScreen(
                        productId = productDetail.productId,
                        userId = userId!!,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}