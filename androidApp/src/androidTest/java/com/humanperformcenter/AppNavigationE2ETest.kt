package com.humanperformcenter

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.or
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

    private object StableTags {
        // Login / onboarding
        const val WELCOME_ACCESS_CTA = "welcome_access_cta"
        const val LOGIN_SCREEN_TITLE = "login_screen_title"
        const val LOGIN_EMAIL_FIELD = "login_email_field"
        const val LOGIN_PASSWORD_FIELD = "login_password_field"
        const val LOGIN_SUBMIT_CTA = "login_submit_cta"
        const val LOGIN_ERROR_INVALID_CREDENTIALS = "login_error_invalid_credentials"

        // Services
        const val SERVICES_TAB_PRODUCTS = "services_tab_products"
        const val SERVICES_TAB_HIRE = "services_tab_hire"
        const val SERVICES_AVAILABLE_ITEM = "services_available_item"
        const val SERVICE_PRODUCT_ITEM = "service_product_item"
        const val SERVICE_PRODUCT_BUY_CTA = "service_product_buy_cta"

        // Bottom tabs
        const val TAB_PRODUCT = "main_tab_product"
        const val TAB_CALENDAR = "main_tab_calendar"
        const val TAB_USER = "main_tab_user"

        // Calendar / profile
        const val CALENDAR_BOOKINGS_SECTION = "calendar_bookings_section"
        const val PROFILE_SECTION = "profile_section"
    }

    private data class StableNode(
        val tag: String,
        val fallbackText: String? = null,
    )

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

        composeRule.waitAndClick(StableNode(StableTags.WELCOME_ACCESS_CTA, "Acceso"))
        composeRule.waitUntilVisible(StableNode(StableTags.LOGIN_SCREEN_TITLE, "Accede a tu cuenta"))

        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_EMAIL_FIELD, "Correo electrónico"), "wrong@user.com")
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_PASSWORD_FIELD, "Contraseña"), "bad")
        composeRule.waitAndClick(StableNode(StableTags.LOGIN_SUBMIT_CTA, "Iniciar sesión"))
        composeRule.waitUntilVisible(StableNode(StableTags.LOGIN_ERROR_INVALID_CREDENTIALS, "Email o contraseña inválidos"))

        composeRule.waitAndClearText(StableNode(StableTags.LOGIN_EMAIL_FIELD, "Correo electrónico"))
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_EMAIL_FIELD, "Correo electrónico"), "valid@humanperform.com")
        composeRule.waitAndClearText(StableNode(StableTags.LOGIN_PASSWORD_FIELD, "Contraseña"))
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_PASSWORD_FIELD, "Contraseña"), "12345678Aa")
        composeRule.waitAndClick(StableNode(StableTags.LOGIN_SUBMIT_CTA, "Iniciar sesión"))

        composeRule.waitUntilVisible(StableNode(StableTags.SERVICES_TAB_PRODUCTS, "Mis productos"))
    }

    @Test
    fun tabs_navigation_services_calendar_user() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClick(StableNode(StableTags.WELCOME_ACCESS_CTA, "Acceso"))
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_EMAIL_FIELD, "Correo electrónico"), "valid@humanperform.com")
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_PASSWORD_FIELD, "Contraseña"), "12345678Aa")
        composeRule.waitAndClick(StableNode(StableTags.LOGIN_SUBMIT_CTA, "Iniciar sesión"))

        composeRule.waitAndClick(StableNode(StableTags.TAB_CALENDAR, "Calendario"))
        composeRule.waitUntilVisible(StableNode(StableTags.CALENDAR_BOOKINGS_SECTION, "Tus sesiones reservadas"))

        composeRule.waitAndClick(StableNode(StableTags.TAB_USER, "Usuario"))
        composeRule.waitUntilVisible(StableNode(StableTags.PROFILE_SECTION, "Mi perfil"))

        composeRule.waitAndClick(StableNode(StableTags.TAB_PRODUCT, "Producto"))
        composeRule.waitUntilVisible(StableNode(StableTags.SERVICES_TAB_PRODUCTS, "Mis productos"))
    }

    @Test
    fun hire_product_and_calendar_booking_section_visible() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClick(StableNode(StableTags.WELCOME_ACCESS_CTA, "Acceso"))
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_EMAIL_FIELD, "Correo electrónico"), "valid@humanperform.com")
        composeRule.waitAndEnterText(StableNode(StableTags.LOGIN_PASSWORD_FIELD, "Contraseña"), "12345678Aa")
        composeRule.waitAndClick(StableNode(StableTags.LOGIN_SUBMIT_CTA, "Iniciar sesión"))

        composeRule.waitAndClick(StableNode(StableTags.SERVICES_TAB_HIRE, "Contratar"))
        composeRule.waitAndClick(StableNode(StableTags.SERVICES_AVAILABLE_ITEM, "Services"))
        composeRule.waitAndClick(StableNode(StableTags.SERVICE_PRODUCT_ITEM, "Pack 8 sesiones"))
        composeRule.waitUntilVisible(StableNode(StableTags.SERVICE_PRODUCT_BUY_CTA, "Comprar"))

        composeRule.waitAndClick(StableNode(StableTags.TAB_CALENDAR, "Calendario"))
        composeRule.waitUntilVisible(StableNode(StableTags.CALENDAR_BOOKINGS_SECTION, "Tus sesiones reservadas"))
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilExists(
        stableNode: StableNode,
        timeoutMillis: Long = 10_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodes(stableNode.matcher(), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitUntilVisible(stableNode: StableNode) {
        waitUntilExists(stableNode)
        onNode(stableNode.matcher(), useUnmergedTree = true).assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitAndClick(stableNode: StableNode) {
        waitUntilVisible(stableNode)
        onNode(stableNode.matcher(), useUnmergedTree = true).performClick()
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitAndEnterText(
        stableNode: StableNode,
        value: String,
    ) {
        waitUntilVisible(stableNode)
        onNode(stableNode.matcher(), useUnmergedTree = true).performTextInput(value)
    }

    private fun androidx.compose.ui.test.junit4.ComposeTestRule.waitAndClearText(stableNode: StableNode) {
        waitUntilVisible(stableNode)
        onNode(stableNode.matcher(), useUnmergedTree = true).performTextClearance()
    }

    private fun StableNode.matcher(): SemanticsMatcher {
        val tagMatcher = hasTestTag(tag)
        val fallbackMatcher = fallbackText?.let { hasText(it, substring = false) }
        return fallbackMatcher?.let { tagMatcher or it } ?: tagMatcher
    }
}
