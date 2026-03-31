import Foundation

enum UITestConfig {
    static let isUITesting = ProcessInfo.processInfo.arguments.contains("-ui-testing")
    static let isMockNetworkEnabled = ProcessInfo.processInfo.environment["MOCK_NETWORK"] == "1"

    static var splashResolvesToLoggedIn: Bool {
        ProcessInfo.processInfo.environment["UI_TEST_SPLASH_LOGGED_IN"] == "1"
    }

    static var forceAuthError: Bool {
        ProcessInfo.processInfo.environment["UI_TEST_FORCE_AUTH_ERROR"] == "1"
    }
}
