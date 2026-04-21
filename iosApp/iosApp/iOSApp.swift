import SwiftUI
import UIKit
import StripePaymentSheet
import shared
import Foundation

@main
struct iOSApp: App {
    init() {
        configureApiBaseUrlForCurrentBuild()
        CryptoCallbacks.register()
        _ = SharedDependencies.shared

        if UITestConfig.isUITesting && UITestConfig.shouldDisableAnimations {
            UIView.setAnimationsEnabled(false)
        }
    }

    private func configureApiBaseUrlForCurrentBuild() {
        let configuredBaseUrl = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String
        HttpClientProviderKt.setApiBaseUrlOverride(baseUrl: configuredBaseUrl)
    }
    
    @StateObject private var appState = AppState()
    
    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appState)
                .onOpenURL { url in
                    _ = StripeAPI.handleURLCallback(with: url)
                }
                .transaction { transaction in
                    if UITestConfig.isUITesting && UITestConfig.shouldDisableAnimations {
                        transaction.disablesAnimations = true
                        transaction.animation = nil
                    }
                }
        }
    }
}
