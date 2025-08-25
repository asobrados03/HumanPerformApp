import SwiftUI

// 1) Enum de rutas para el NavigationStack
enum Route: Hashable {
    case welcome
    case register
    case login
    case service
}

// 2) Root con un único NavigationStack y path
struct RootView: View {
    @State private var path: [Route] = []

    var body: some View {
        NavigationStack(path: $path) {
            WelcomeView(path: $path)
                .navigationDestination(for: Route.self) { route in
                switch route {
                    case .welcome:
                        WelcomeView(path: $path)
                    case .register:
                        RegisterView(onNavigateToLogin: { path = [.login] })
                    case .login:
                        LoginView(
                            onSuccess: { path = [.service] },
                            onForgot: { },
                            onRegister: { path = [.register] }
                        )
                    case .service:
                        ServicesView()
                }
            }
        }
    }
}
