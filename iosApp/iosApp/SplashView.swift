//
//  SplashView.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct SplashView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var didResolve = false
    @State private var fallbackTask: Task<Void, Never>?

    let onResolved: (_ isLoggedIn: Bool) -> Void

    var body: some View {
        ZStack {
            // UI Tests: evitar dependencias de red en splash
            Color(.systemBackground).ignoresSafeArea()
            ProgressView("Cargando…")
        }
        .accessibilityIdentifier("splashView")
        .onAppear {
            fallbackTask?.cancel()
            fallbackTask = Task {
                try? await Task.sleep(nanoseconds: 3_000_000_000)
                guard !Task.isCancelled else { return }
                resolveOnce(sessionVM.isLoggedIn?.boolValue ?? false)
            }

            if UITestConfig.isUITesting {
                resolveOnce(UITestConfig.splashResolvesToLoggedIn)
                return
            }

            // Importante: el primer valor de StateFlow puede llegar antes
            // de que SwiftUI conecte el onChange, por lo que lo resolvemos aquí.
            if let isLogged = sessionVM.isLoggedIn {
                resolveOnce(isLogged.boolValue)
            }
        }
        .onChange(of: sessionVM.isLoggedIn) { value in
            guard !UITestConfig.isUITesting else { return }
            guard let value else { return }
            resolveOnce(value.boolValue)
        }
        .onDisappear {
            fallbackTask?.cancel()
        }
    }

    private func resolveOnce(_ isLoggedIn: Bool) {
        guard !didResolve else { return }
        didResolve = true
        fallbackTask?.cancel()
        onResolved(isLoggedIn)
    }
}
