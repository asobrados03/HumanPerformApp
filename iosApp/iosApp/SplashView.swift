//
//  SplashView.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct SplashView: View {
    @StateViewModel private var sessionVM = makeUserSessionViewModel()
    let onResolved: (_ isLoggedIn: Bool) -> Void

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()
            ProgressView("Cargando…")
        }
                .onChange(of: sessionVM.isLoggedIn) { value in
            guard let value else { return }
            onResolved(value)
        }
    }
}
