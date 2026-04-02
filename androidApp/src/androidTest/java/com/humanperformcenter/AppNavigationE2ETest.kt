package com.humanperformcenter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.humanperformcenter.app.MainActivity
import com.humanperformcenter.app.TestOverrides
import com.humanperformcenter.shared.data.network.HttpClientProvider
import com.humanperformcenter.shared.presentation.di.appModule
import com.humanperformcenter.shared.presentation.di.platformModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
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

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Before
    fun setup() {
        stopKoin()
        val mockProvider = MockHttpClientProvider()
        TestOverrides.httpClientProviderOverride = mockProvider
        startKoin {
            modules(
                appModule,
                platformModule,
                module {
                    single<HttpClientProvider> { mockProvider }
                }
            )
            allowOverride(true)
        }
    }

    @After
    fun tearDown() {
        TestOverrides.reset()
        stopKoin()
    }

    @Test
    fun splash_to_welcome_and_login_invalid_valid() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClick(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitUntilVisible(StableTags.LOGIN_SCREEN_TITLE)

        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "wrong@user.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "bad")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)
        composeRule.waitUntilVisible(StableTags.LOGIN_ERROR_INVALID_CREDENTIALS)

        composeRule.waitAndClearText(StableTags.LOGIN_EMAIL_FIELD)
        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
        composeRule.waitAndClearText(StableTags.LOGIN_PASSWORD_FIELD)
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)

        composeRule.waitUntilVisible(StableTags.SERVICES_TAB_PRODUCTS)
    }

    @Test
    fun tabs_navigation_services_calendar_user() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClick(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)

        composeRule.waitAndClick(StableTags.TAB_CALENDAR)
        composeRule.waitUntilVisible(StableTags.CALENDAR_BOOKINGS_SECTION)

        composeRule.waitAndClick(StableTags.TAB_USER)
        composeRule.waitUntilVisible(StableTags.PROFILE_SECTION)

        composeRule.waitAndClick(StableTags.TAB_PRODUCT)
        composeRule.waitUntilVisible(StableTags.SERVICES_TAB_PRODUCTS)
    }

    @Test
    fun hire_product_and_calendar_booking_section_visible() {
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClick(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)

        composeRule.waitAndClick(StableTags.SERVICES_TAB_HIRE)
        composeRule.waitAndClick(StableTags.SERVICES_AVAILABLE_ITEM)
        composeRule.waitAndClick(StableTags.SERVICE_PRODUCT_ITEM)
        composeRule.waitUntilVisible(StableTags.SERVICE_PRODUCT_BUY_CTA)

        composeRule.waitAndClick(StableTags.TAB_CALENDAR)
        composeRule.waitUntilVisible(StableTags.CALENDAR_BOOKINGS_SECTION)
    }

    private fun ComposeTestRule.waitUntilExists(
        tag: String,
        timeoutMillis: Long = 10_000,
    ) {
        waitUntil(timeoutMillis) {
            onAllNodes(hasTestTag(tag))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun ComposeTestRule.waitUntilVisible(tag: String) {
        waitUntilExists(tag)

        onNode(hasTestTag(tag))
            .assertIsDisplayed()
    }

    private fun ComposeTestRule.waitAndClick(tag: String) {
        waitUntilExists(tag)

        onNode(hasTestTag(tag) and hasClickAction())
            .assertIsDisplayed()
            .performClick()
    }

    private fun ComposeTestRule.waitAndEnterText(
        tag: String,
        value: String,
    ) {
        waitUntilExists(tag)

        onNode(hasTestTag(tag))
            .assertIsDisplayed()
            .performClick()
            .performTextInput(value)
    }

    private fun ComposeTestRule.waitAndClearText(tag: String) {
        waitUntilExists(tag)

        onNode(hasTestTag(tag))
            .assertIsDisplayed()
            .performTextClearance()
    }
}
