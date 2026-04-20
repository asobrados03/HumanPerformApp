import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct RootView: View {
    @EnvironmentObject var appState: AppState
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var hasResolved = false

    var body: some View {
        Group {
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
        .onAppear {
            syncAuthenticationState(from: sessionVM.isLoggedIn)
        }
        .onChange(of: sessionVM.isLoggedIn) { value in
            syncAuthenticationState(from: value)
        }
    }

    private func syncAuthenticationState(from value: KotlinBoolean?) {
        guard let value else { return }
        appState.isAuthenticated = value.boolValue
    }
}
