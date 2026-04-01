import XCTest

class BaseUITestCase: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @discardableResult
    func makeConfiguredApp() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-ui-testing", "-ui-testing-disable-animations"]
        app.launchEnvironment["MOCK_NETWORK"] = "1"
        app.launchEnvironment["UI_TEST_DISABLE_ANIMATIONS"] = "1"
        return app
    }
}
