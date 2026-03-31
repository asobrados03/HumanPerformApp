package com.humanperformcenter

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.humanperformcenter.app.MainActivity
import com.humanperformcenter.app.TestOverrides
import com.humanperformcenter.shared.presentation.di.networkModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class AppNavigationE2ETest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Before
    fun setup() {
        val mockProvider = MockHttpClientProvider()
        TestOverrides.httpClientProviderOverride = mockProvider
        loadKoinModules(
            module {
                includes(networkModule)
                single(override = true) { mockProvider }
            }
        )
    }

    @After
    fun tearDown() {
        TestOverrides.reset()
    }

    @Test
    fun splash_to_welcome_and_login_invalid_valid() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.onNodeWithText("Acceso").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Accede a tu cuenta").assertIsDisplayed()

        composeRule.onNodeWithText("Correo electrónico").performTextInput("wrong@user.com")
        composeRule.onNodeWithText("Contraseña").performTextInput("bad")
        composeRule.onNodeWithText("Iniciar sesión").performClick()
        composeRule.onNodeWithText("Email o contraseña inválidos").assertIsDisplayed()

        composeRule.onNodeWithText("Correo electrónico").performTextClearance()
        composeRule.onNodeWithText("Correo electrónico").performTextInput("valid@humanperform.com")
        composeRule.onNodeWithText("Contraseña").performTextClearance()
        composeRule.onNodeWithText("Contraseña").performTextInput("12345678Aa")
        composeRule.onNodeWithText("Iniciar sesión").performClick()

        composeRule.onNodeWithText("Mis productos").assertIsDisplayed()
    }

    @Test
    fun tabs_navigation_services_calendar_user() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.onNodeWithText("Acceso").performClick()
        composeRule.onNodeWithText("Correo electrónico").performTextInput("valid@humanperform.com")
        composeRule.onNodeWithText("Contraseña").performTextInput("12345678Aa")
        composeRule.onNodeWithText("Iniciar sesión").performClick()

        composeRule.onNodeWithText("Calendario").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Tus sesiones reservadas").assertIsDisplayed()

        composeRule.onNodeWithText("Usuario").performClick()
        composeRule.onNodeWithText("Mi perfil").assertIsDisplayed()

        composeRule.onNodeWithText("Producto").performClick()
        composeRule.onNodeWithText("Mis productos").assertIsDisplayed()
    }

    @Test
    fun hire_product_and_calendar_booking_section_visible() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.onNodeWithText("Acceso").performClick()
        composeRule.onNodeWithText("Correo electrónico").performTextInput("valid@humanperform.com")
        composeRule.onNodeWithText("Contraseña").performTextInput("12345678Aa")
        composeRule.onNodeWithText("Iniciar sesión").performClick()

        composeRule.onNodeWithText("Contratar").performClick()
        composeRule.onNodeWithText("Services").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Pack 8 sesiones").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Comprar").assertIsDisplayed()

        composeRule.onNodeWithText("Calendario").performClick()
        composeRule.onNodeWithText("Tus sesiones reservadas").assertIsDisplayed()
    }
}
