//
//  AuthFlow.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

enum AuthRoute: Hashable { case welcome, register, login }

struct AuthFlow: View {
    let onLoginSuccess: () -> Void
    @State private var path: [AuthRoute] = [.welcome]

    var body: some View {
        NavigationStack(path: $path) {
            WelcomeView(
                onLogin:   { path = [.login] },
                onRegister:{ path = [.register] }
            )
            .navigationDestination(for: AuthRoute.self) { route in
                switch route {
                case .welcome:
                    WelcomeView(
                        onLogin:   { path = [.login] },
                        onRegister:{ path = [.register] }
                    )
                case .register:
                    RegisterView(onNavigateToLogin: { path = [.login] })
                case .login:
                    LoginView(
                        onSuccess: {
                            // aquí puedes guardar token en SecureStorage y el flow emitirá true
                            onLoginSuccess()
                        },
                        onForgot:  { /* ... */ },
                        onRegister:{ path = [.register] }
                    )
                }
            }
        }
    }
}
