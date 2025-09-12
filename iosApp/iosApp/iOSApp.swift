import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        let prefs = DataStoreProvider().get()
        CryptoCallbacks.register()
        SecureStorage.shared.initialize(prefs: prefs)
    }
    
    @StateObject private var appState = AppState()
    
    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appState)
        }
    }
}
