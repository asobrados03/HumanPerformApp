import SwiftUI
import UIKit
import StripePaymentSheet
import shared

@main
struct iOSApp: App {
    init() {
        _ = SharedDependencies.shared
        let prefs = DataStoreProvider().get()
        CryptoCallbacks.register()
        SecureStorage.shared.initialize(prefs: prefs)
        if UITestConfig.isUITesting && UITestConfig.shouldDisableAnimations {
            UIView.setAnimationsEnabled(false)
        }
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
