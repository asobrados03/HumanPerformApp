import SwiftUI

struct RootView: View {
    @EnvironmentObject var appState: AppState
    @State private var hasResolved = false

    var body: some View {
        if !hasResolved {
            SplashView { isLogged in
                appState.isAuthenticated = isLogged
                hasResolved = true
            }
        } else {
            if appState.isAuthenticated {
                MainTabs()
                    .accessibilityIdentifier("mainTabs")
            } else {
                AuthFlow(onLoginSuccess: { appState.isAuthenticated = true })
                    .accessibilityIdentifier("authFlow")
            }
        }
    }
}
