//
//  SplashView.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

struct SplashView: View {
    @State private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    let onResolved: (_ isLoggedIn: Bool) -> Void

    var body: some View {
        ZStack {
            // UI Tests: evitar dependencias de red en splash
            Color(.systemBackground).ignoresSafeArea()
            ProgressView("Cargando…")
        }
                .accessibilityIdentifier("splashView")
        .onAppear {
            if UITestConfig.isUITesting {
                onResolved(UITestConfig.splashResolvesToLoggedIn)
            }
        }
        .onChange(of: sessionVM.isLoggedIn) { value in
            guard !UITestConfig.isUITesting else { return }
            guard let value else { return }
            onResolved(value)
        }
    }
}
