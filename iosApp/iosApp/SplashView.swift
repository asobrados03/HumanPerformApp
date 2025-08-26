//
//  SplashView.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct SplashView: View {
    @StateObject private var vm = SessionViewModel()
    let onResolved: (_ isLoggedIn: Bool) -> Void

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()
            ProgressView("Cargando…")
        }
        .task { vm.startObserving() }
        .onChange(of: vm.isLoggedIn) { value in
            guard let value else { return }
            onResolved(value)
        }
    }
}
