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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.humanperformcenter.app.SetStatusBarColor
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.presentation.viewmodel.AuthViewModel
import com.humanperformcenter.shared.presentation.viewmodel.DaySessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.StripeViewModel
import com.humanperformcenter.shared.presentation.viewmodel.ServiceProductViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserStatsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserBookingsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserDocumentsViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserSessionViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserWalletViewModel
import com.humanperformcenter.shared.presentation.viewmodel.UserViewModel
import com.humanperformcenter.ui.components.app.FullScreenLoading
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
import com.humanperformcenter.ui.screens.ProductDetailScreen
import com.humanperformcenter.ui.screens.ActiveProductDetailScreen
import com.humanperformcenter.ui.screens.PaymentSuccessScreen
import com.humanperformcenter.ui.screens.RegisterScreen
import com.humanperformcenter.ui.screens.ServicesScreen
import com.humanperformcenter.ui.screens.SplashScreen
import com.humanperformcenter.ui.screens.StripeSinglePaymentScreen
import com.humanperformcenter.ui.screens.StripeSubscriptionScreen
import com.humanperformcenter.ui.screens.UserScreen
import com.humanperformcenter.ui.screens.UserStatsScreen
import com.humanperformcenter.ui.screens.ViewPaymentMethodScreen
import com.humanperformcenter.ui.screens.WelcomeScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    onPlaySound: (Int) -> Unit
) {
    SetStatusBarColor(
        statusBarColor = Color(0xFFB71C1C),
        navigationBarColor = Color(0xFFB71C1C)
    )

    val stripeViewModel: StripeViewModel = koinViewModel()

    val userViewModel: UserViewModel = koinViewModel()

    val authViewModel: AuthViewModel = koinViewModel()

    val serviceProductViewModel: ServiceProductViewModel = koinViewModel()

    val isLoggedIn by userViewModel.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = null)

    val userData by userViewModel.userData.collectAsStateWithLifecycle()

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
                SplashScreen(navController, userViewModel)
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
                    userViewModel = userViewModel,
                    stripeViewModel = stripeViewModel,
                    serviceProductViewModel = serviceProductViewModel
                )
            }
            composable<HireProduct> { backStackEntry ->
                // Reconstruimos el objeto ServicioRoute
                val route = backStackEntry.toRoute<HireProduct>()
                val user = userViewModel.userData.collectAsStateWithLifecycle().value
                val userId = user?.id

                // Sólo mostramos si tenemos usuario
                if (userId != null) {
                    HireProductScreen(
                        serviceId     = route.serviceId,
                        navController = navController,
                        serviceProductViewModel = serviceProductViewModel,
                        userData = userData
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
                ViewPaymentMethodScreen(
                    navController = navController,
                    stripeViewModel = stripeViewModel
                )
            }
            composable<AddCoupon>{
                when (val user = userData) {
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
                val userSessionViewModel: UserSessionViewModel = koinViewModel()
                ConfigurationRoute(
                    navController = navController,
                    userSessionViewModel = userSessionViewModel
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
                val userDocumentsViewModel: UserDocumentsViewModel = koinViewModel()
                DocumentScreen(
                    navController = navController,
                    userId = userData?.id,
                    userDocumentsViewModel = userDocumentsViewModel
                )
            }
            composable<ElectronicWallet> {
                val userWalletViewModel: UserWalletViewModel = koinViewModel()
                ElectronicWalletScreen(
                    navController = navController,
                    userWalletViewModel = userWalletViewModel,
                    userId = userData?.id ?: 0
                )
            }
            composable<StripeSinglePayment> { backStackEntry ->
                val route = backStackEntry.toRoute<StripeSinglePayment>()
                StripeSinglePaymentScreen(
                    navController = navController,
                    stripeViewModel = stripeViewModel,
                    userId = userData?.id ?: 0,
                    productPrice = route.productPrice,
                    productId = route.productId,
                    couponCode = route.couponCode,
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }
            composable<StripeSubscription> { backStackEntry ->
                val route = backStackEntry.toRoute<StripeSubscription>()
                StripeSubscriptionScreen(
                    navController = navController,
                    stripeViewModel = stripeViewModel,
                    userId = userData?.id ?: 0,
                    productPrice = route.productPrice,
                    productId = route.productId,
                    priceId = route.priceId,
                    couponCode = route.couponCode,
                    onClose = { navController.popBackStack() }
                )
            }
            composable<PaymentSuccess> {
                PaymentSuccessScreen(
                    onContinueShopping = {
                        navController.navigate(Service) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<ProductDetail> {
                ProductDetailScreen(
                    navController = navController,
                    serviceProductViewModel = serviceProductViewModel,
                    userId = userData?.id ?: -1
                )
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
                    navController = navController
                )
            }

            composable<Calendar> {
                val daySessionViewModel: DaySessionViewModel = koinViewModel()
                val userSessionViewModel: UserSessionViewModel = koinViewModel()
                val userBookingsViewModel: UserBookingsViewModel = koinViewModel()

                CalendarScreen(
                    navController = navController,
                    serviceProductViewModel = serviceProductViewModel,
                    userSessionViewModel = userSessionViewModel,
                    userBookingsViewModel = userBookingsViewModel,
                    onPlaySound = onPlaySound,
                    daySessionViewModel = daySessionViewModel
                )
            }
            composable<Stats> {
                val statsViewModel: UserStatsViewModel = koinViewModel()
                val userId = userData?.id

                LaunchedEffect(userId) {
                    if (userId != null && userId > 0) {
                        statsViewModel.loadStatistics(userId)
                    }
                }

                UserStatsScreen(
                    navController = navController,
                    statsViewModel = statsViewModel,
                    onRetry = {
                        if (userId != null && userId > 0) {
                            statsViewModel.loadStatistics(userId)
                        }
                    }
                )
            }
            composable<ActiveProductDetail> { backStackEntry ->
                val activeProductDetail = backStackEntry.toRoute<ActiveProductDetail>()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Service)
                }
                val viewModel: ServiceProductViewModel = koinViewModel(
                    viewModelStoreOwner = parentEntry
                )
                val userId = userData?.id

                if (userId != null) {
                    ActiveProductDetailScreen(
                        productId = activeProductDetail.productId,
                        userId = userId,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}