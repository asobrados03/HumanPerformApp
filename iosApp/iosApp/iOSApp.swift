import SwiftUI
import UIKit
import StripePaymentSheet
import shared
import Foundation

@main
struct iOSApp: App {
    // ✅ iOSApp.swift corregido
    init() {
        print(">>> 1. INIT START")
        configureApiBaseUrlForCurrentBuild()
        print(">>> 2. CRYPTO REGISTER START")
        CryptoCallbacks.register()
        print(">>> 3. CRYPTO REGISTER DONE")
        _ = SharedDependencies.shared
        print(">>> 4. KOIN READY")
        
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
