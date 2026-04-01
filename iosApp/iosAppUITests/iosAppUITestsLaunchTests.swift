import XCTest

final class iosAppUITestsLaunchTests: BaseUITestCase {
    override class var runsForEachTargetApplicationUIConfiguration: Bool {
        true
    }

    override func setUpWithError() throws {
        try super.setUpWithError()
    }

    func testLaunch() throws {
        let app = makeConfiguredApp()
        app.launchEnvironment["UI_TEST_SPLASH_LOGGED_IN"] = "1"
        app.launch()

        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "Launch Screen"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
