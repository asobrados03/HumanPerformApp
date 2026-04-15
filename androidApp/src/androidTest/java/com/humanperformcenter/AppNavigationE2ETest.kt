package com.humanperformcenter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
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
import org.koin.android.ext.koin.androidContext

@RunWith(AndroidJUnit4::class)
class AppNavigationE2ETest {

    private object StableTags {
        // Login / onboarding
        const val WELCOME_ACCESS_CTA = "welcome_access_cta"
        const val LOGIN_SCREEN_TITLE = "login_screen_title"
        const val LOGIN_EMAIL_FIELD = "login_email_field"
        const val LOGIN_PASSWORD_FIELD = "login_password_field"
        const val LOGIN_SUBMIT_CTA = "login_submit_cta"
        const val LOGIN_ERROR_MESSAGE = "login_error_message"
        const val LOGIN_LOADING_INDICATOR = "login_loading_indicator"

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
        setupWithScenario(MockHttpClientProvider.Scenario.HappyPath)
    }

    private fun setupWithScenario(scenario: MockHttpClientProvider.Scenario) {
        stopKoin()
        val mockProvider = MockHttpClientProvider(scenario)
        TestOverrides.httpClientProviderOverride = mockProvider
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
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

        composeRule.waitAndClickIfExists(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitUntilVisible(StableTags.LOGIN_SCREEN_TITLE)

        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "wrong@user.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "bad")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)
        composeRule.waitUntilVisible(StableTags.LOGIN_ERROR_MESSAGE)

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

        composeRule.loginIfNeeded()

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

        composeRule.loginIfNeeded()

        composeRule.waitAndClick(StableTags.SERVICES_TAB_HIRE)
        composeRule.waitAndClick(StableTags.SERVICES_AVAILABLE_ITEM)
        composeRule.waitAndClick(StableTags.SERVICE_PRODUCT_ITEM)
        composeRule.waitUntilVisible(StableTags.SERVICE_PRODUCT_BUY_CTA)

        // ProductDetailScreen does not render the bottom navigation bar,
        // so we return to Services before switching to Calendar.
        pressBack()
        composeRule.waitUntilVisible(StableTags.SERVICES_TAB_PRODUCTS)

        composeRule.waitAndClick(StableTags.TAB_CALENDAR)
        composeRule.waitUntilVisible(StableTags.CALENDAR_BOOKINGS_SECTION)
    }

    @Test
    fun login_when_server_returns_500_shows_error_and_hides_loading_indicator() {
        setupWithScenario(MockHttpClientProvider.Scenario.LoginServerError)
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClickIfExists(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)

        composeRule.waitUntilVisible(StableTags.LOGIN_ERROR_MESSAGE)
        composeRule.onAllNodes(hasTestTag(StableTags.LOGIN_LOADING_INDICATOR))
            .assertCountEquals(0)
    }

    @Test
    fun session_expired_while_loading_products_redirects_back_to_login() {
        setupWithScenario(MockHttpClientProvider.Scenario.SessionExpiredOnProducts)
        ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitAndClickIfExists(StableTags.WELCOME_ACCESS_CTA)
        composeRule.waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
        composeRule.waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
        composeRule.waitAndClick(StableTags.LOGIN_SUBMIT_CTA)

        composeRule.waitUntilVisible(StableTags.WELCOME_ACCESS_CTA)
    }

    private fun ComposeTestRule.waitUntilExists(
        tag: String,
        timeoutMillis: Long = 20_000,
    ) {
        waitForIdle()
        waitUntil(timeoutMillis) {
            hasAnyNodeWithTag(tag)
        }
    }

    private fun ComposeTestRule.hasAnyNodeWithTag(tag: String): Boolean {
        return onAllNodes(hasTestTag(tag))
            .fetchSemanticsNodes().isNotEmpty() ||
            onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
    }

    private fun ComposeTestRule.waitUntilVisible(tag: String) {
        waitUntilExists(tag)

        onNode(hasTestTag(tag))
            .assertIsDisplayed()
    }

    private fun ComposeTestRule.waitAndClick(tag: String) {
        waitUntilExists(tag)

        val mergedClickableNodes = onAllNodes(hasTestTag(tag) and hasClickAction())
            .fetchSemanticsNodes()

        if (mergedClickableNodes.isNotEmpty()) {
            onNode(hasTestTag(tag) and hasClickAction())
                .assertIsDisplayed()
                .performClick()
        } else {
            onNode(hasTestTag(tag), useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
        }
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

    private fun ComposeTestRule.waitAndClickIfExists(
        tag: String,
        timeoutMillis: Long = 1_500,
    ): Boolean {
        waitForIdle()
        val startTimeMillis = System.currentTimeMillis()
        while (!hasAnyNodeWithTag(tag) &&
            System.currentTimeMillis() - startTimeMillis < timeoutMillis
        ) {
            Thread.sleep(100)
            waitForIdle()
        }

        if (!hasAnyNodeWithTag(tag)) return false
        waitAndClick(tag)
        return true
    }

    private fun ComposeTestRule.loginIfNeeded() {
        waitAndClickIfExists(StableTags.WELCOME_ACCESS_CTA)

        if (hasAnyNodeWithTag(StableTags.LOGIN_EMAIL_FIELD)) {
            waitAndEnterText(StableTags.LOGIN_EMAIL_FIELD, "valid@humanperform.com")
            waitAndEnterText(StableTags.LOGIN_PASSWORD_FIELD, "12345678Aa")
            waitAndClick(StableTags.LOGIN_SUBMIT_CTA)
        }

        waitUntilVisible(StableTags.SERVICES_TAB_PRODUCTS)
    }
}
