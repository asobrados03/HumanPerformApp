//
//  ConfigurationView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI

/// Pantalla de configuración del usuario.
struct ConfigurationView: View {
    @EnvironmentObject var userVM: UserViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Configuración")
                .font(.title2)
                .fontWeight(.semibold)
            if let user = userVM.currentUser {
                Text(user.fullName)
            }
            Spacer()
        }
        .padding()
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}

