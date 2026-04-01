import XCTest

final class iosAppUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments.append("-ui-testing")
        app.launchEnvironment["MOCK_NETWORK"] = "1"
    }

    func testAuthFlowSplashWelcomeLoginRegister() {
        // Arrange
        // Act
        app.launchEnvironment["UI_TEST_SPLASH_LOGGED_IN"] = "0"
        app.launch()

        // Assert
        XCTAssertTrue(app.otherElements["welcomeView"].waitForExistence(timeout: 5))

        app.buttons["welcomeRegisterButton"].tap()
        XCTAssertTrue(app.otherElements["registerView"].waitForExistence(timeout: 5))

        app.navigationBars.buttons.element(boundBy: 0).tap()
        XCTAssertTrue(app.otherElements["welcomeView"].waitForExistence(timeout: 3))

        app.buttons["welcomeLoginButton"].tap()
        XCTAssertTrue(app.otherElements["loginView"].waitForExistence(timeout: 5))
    }

    func testMainNavigationLoadsServicesCalendarAndProfile() {
        // Arrange
        // Act
        app.launchEnvironment["UI_TEST_SPLASH_LOGGED_IN"] = "1"
        app.launch()

        // Assert
        XCTAssertTrue(app.otherElements["mainTabs"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements["servicesView"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Mock Services Loaded"].waitForExistence(timeout: 5))

        app.tabBars.buttons["Calendario"].tap()
        XCTAssertTrue(app.staticTexts["Mock Calendar Loaded"].waitForExistence(timeout: 5))

        app.tabBars.buttons["Usuario"].tap()
        XCTAssertTrue(app.staticTexts["Mock User Loaded"].waitForExistence(timeout: 5))

        app.buttons["myProfileButton"].tap()
        XCTAssertTrue(app.otherElements["myProfileView"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements["myProfileLoadedMarker"].waitForExistence(timeout: 5))
    }

    func testAuthenticationErrorIsVisibleOnUI() {
        // Arrange
        // Act
        app.launchEnvironment["UI_TEST_SPLASH_LOGGED_IN"] = "0"
        app.launchEnvironment["UI_TEST_FORCE_AUTH_ERROR"] = "1"
        app.launch()

        // Assert
        XCTAssertTrue(app.otherElements["welcomeView"].waitForExistence(timeout: 5))
        app.buttons["welcomeLoginButton"].tap()
        XCTAssertTrue(app.otherElements["loginView"].waitForExistence(timeout: 5))

        let email = app.textFields["loginEmailField"]
        XCTAssertTrue(email.waitForExistence(timeout: 3))
        email.tap()
        email.typeText("error@mock.com")

        let password = app.secureTextFields["loginPasswordField"]
        XCTAssertTrue(password.waitForExistence(timeout: 3))
        password.tap()
        password.typeText("wrong-password")

        app.buttons["loginSubmitButton"].tap()
        XCTAssertTrue(app.staticTexts["Credenciales inválidas (mock)"].waitForExistence(timeout: 5))
    }
}
