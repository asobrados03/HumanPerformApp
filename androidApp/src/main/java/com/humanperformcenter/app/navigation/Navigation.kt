package com.humanperformcenter.app.navigation

import com.humanperformcenter.ui.viewmodel.DaySessionViewModelFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.humanperformcenter.app.SetStatusBarColor
import com.humanperformcenter.di.AppModule
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.ui.components.FullScreenLoading
import com.humanperformcenter.ui.components.FullScreenTextLoading
import com.humanperformcenter.ui.screens.BlogDetailScreen
import com.humanperformcenter.ui.screens.CalendarScreen
import com.humanperformcenter.ui.screens.ChangePasswordScreen
import com.humanperformcenter.ui.screens.ChatScreen
import com.humanperformcenter.ui.screens.HireProductScreen
import com.humanperformcenter.ui.screens.DocumentScreen
import com.humanperformcenter.ui.screens.FavoritesScreen
import com.humanperformcenter.ui.screens.LoginScreen
import com.humanperformcenter.ui.screens.MyProfileScreen
import com.humanperformcenter.ui.screens.NewBlogScreen
import com.humanperformcenter.ui.screens.NewProductScreen
import com.humanperformcenter.ui.screens.PaymentScreen
import com.humanperformcenter.ui.screens.ProductDetailScreen
import com.humanperformcenter.ui.screens.RegisterScreen
import com.humanperformcenter.ui.screens.SplashScreen
import com.humanperformcenter.ui.screens.UserScreen
import com.humanperformcenter.ui.screens.ViewPaymentScreen
import com.humanperformcenter.ui.screens.WelcomeScreen
import com.humanperformcenter.ui.viewmodel.AuthViewModel
import com.humanperformcenter.ui.viewmodel.AuthViewModelFactory
import com.humanperformcenter.ui.viewmodel.BlogViewModel
import com.humanperformcenter.ui.viewmodel.BlogViewModelFactory
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModel
import com.humanperformcenter.ui.viewmodel.ServiceProductViewModelFactory
import com.humanperformcenter.ui.viewmodel.DaySessionViewModel
import com.humanperformcenter.ui.viewmodel.SessionViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModel
import com.humanperformcenter.ui.viewmodel.UserViewModelFactory
import com.humanperformcenter.ui.viewmodel.state.BlogDetailState
import com.humanperformcenter.ui.viewmodel.state.ChangePasswordState
import com.humanperformcenter.ui.viewmodel.state.CoachState

@Composable
fun Navigation(
    sessionViewModel: SessionViewModel,
    navController: NavHostController,
    onPlaySound: (Int) -> Unit
) {
    SetStatusBarColor(
        statusBarColor = Color(0xFFB71C1C),
        navigationBarColor = Color(0xFFB71C1C)
    )

    val isLoggedIn by sessionViewModel
        .isLoggedInFlow
        .collectAsState(initial = false)

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
                        navController.navigate(Register) {
                            popUpTo(Welcome) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Login) {
                            popUpTo(Welcome) { inclusive = true }
                        }
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
                        navController.navigate(Login) {
                            popUpTo(Register) { inclusive = true }
                        }
                    },
                    navController = navController
                )
            }
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NewProduct) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Register) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    navController = navController
                )
            }
            composable<NewProduct> {
                NewProductScreen(
                    navController = navController,
                    sessionViewModel = sessionViewModel,
                    userViewModel = viewModel(
                        factory = UserViewModelFactory(AppModule.userUseCase)
                    ),
                    serviceProductViewModel = viewModel(
                        factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase)
                    )
                )
            }
            composable("servicio/{serviceId}") { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("serviceId")?.toIntOrNull()
                val userIdState = sessionViewModel.userId.collectAsState()
                val userId = userIdState.value
                val viewModel: ServiceProductViewModel =
                    viewModel(factory = ServiceProductViewModelFactory(AppModule.serviceProductUseCase))

                serviceId?.let {
                    HireProductScreen(
                        serviceId = it,
                        navController = navController,
                        viewModel = viewModel,
                        userId = userId!!
                    )
                }
            }
            composable<User> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )
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
                        // Ya cargó y NO hay user: navegar a Login
                        LaunchedEffect(Unit) {
                            navController.navigate(Login) {
                                popUpTo(User) { inclusive = true }
                            }
                        }
                    }

                    else -> {
                        UserScreen(
                            navController = navController,
                            user = user!!,
                            onEditProfile = { navController.navigate(EditProfile) },
                            onViewProfile = { navController.navigate(MyProfile) },
                            onMenuClick = { option ->
                                when (option) {
                                    MenuOption.FAVORITOS -> navController.navigate(Favorites)
                                    MenuOption.CHAT -> navController.navigate(Chat)
                                    MenuOption.DOCUMENTO -> navController.navigate(Document)
                                    MenuOption.PAGO -> navController.navigate(Payment)
                                    MenuOption.VER_PAGO -> navController.navigate(ViewPayment)
                                    MenuOption.CONFIGURACION -> navController.navigate(Configuration)
                                }
                            }
                        )
                    }
                }
            }
            composable<Configuration> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )

                ConfigurationRoute(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
            composable<ChangePassword> {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(AppModule.authUseCase)
                )

                val changePasswordState by authViewModel.isChangingPassword.observeAsState(
                    ChangePasswordState.Idle
                )

                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )
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
            composable<Chat> {
                ChatScreen(navController = navController)
            }
            composable<Document> {
                DocumentScreen(navController = navController)
            }
            composable<Payment> {
                PaymentScreen(navController = navController)
            }
            composable<ViewPayment> {
                ViewPaymentScreen(navController = navController)
            }
            composable<EditProfile> {
                EditProfileRoute(navController = navController)
            }
            composable<MyProfile> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )
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
            composable<Favorites> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )
                val coachState by userViewModel.coachesState.collectAsState()
                val favoriteId by userViewModel.favoriteCoachId.collectAsState()

                LaunchedEffect(Unit) {
                    userViewModel.getCoaches()
                }

                when (coachState) {
                    is CoachState.Loading -> {
                        FullScreenTextLoading("Cargando entrenadores...", PaddingValues(16.dp))
                    }

                    is CoachState.Success -> {
                        FavoritesScreen(
                            coaches = (coachState as CoachState.Success).coaches,
                            selectedCoachId = favoriteId,
                            onSelect = { prof ->
                                userViewModel.markFavorite(prof.id)
                            },
                            navController = navController
                        )
                    }

                    is CoachState.Error -> {
                        // Muestra un mensaje de error
                        val message = (coachState as CoachState.Error).message
                        Text("Error: $message")
                    }

                    CoachState.Idle -> {
                        FullScreenLoading()
                    }
                }
            }

            composable<Calendar> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(AppModule.userUseCase)
                )
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
            composable<NewBlog> {
                val blogViewModel: BlogViewModel = viewModel(
                    factory = BlogViewModelFactory(AppModule.blogUseCase)
                )
                val state by blogViewModel.state.collectAsState()
                LaunchedEffect(Unit) { blogViewModel.loadBlogs() }

                NewBlogScreen(
                    navController = navController,
                    blogState = state,
                    onEntryClick = { entry ->
                        navController.navigate(BlogDetail(entry.blogId))
                    },
                    onRetry = { blogViewModel.loadBlogs() }
                )
            }
            composable<BlogDetail> { backStack ->
                val blogDetail = backStack.toRoute<BlogDetail>()
                val parentEntry = remember(backStack) {
                    navController.getBackStackEntry(NewBlog)
                }
                val blogViewModel: BlogViewModel = viewModel(
                    parentEntry,
                    factory = BlogViewModelFactory(AppModule.blogUseCase)
                )
                val detailState by blogViewModel.detailState.collectAsState() // Observar el estado del detalle

                // Disparar la carga del detalle
                LaunchedEffect(blogDetail.blogId) {
                    blogViewModel.loadBlogDetail(blogDetail.blogId)
                }

                BlogDetailScreen(
                    navController = navController,
                    blog = (detailState as? BlogDetailState.Success)?.blog,
                    isLoading = detailState is BlogDetailState.Loading,
                    errorMessage = (detailState as? BlogDetailState.Error)?.message,
                    onRetry = { blogViewModel.loadBlogDetail(blogDetail.blogId) }
                )
            }
            composable<ProductDetail> { backStackEntry ->
                val productDetail = backStackEntry.toRoute<ProductDetail>()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NewProduct)
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