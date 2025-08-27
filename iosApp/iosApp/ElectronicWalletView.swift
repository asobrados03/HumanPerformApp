//
//  ElectronicWalletView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI

/// Pantalla del monedero virtual del usuario.
struct ElectronicWalletView: View {
    @EnvironmentObject var userVM: UserViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Monedero Virtual")
                .font(.title2)
                .fontWeight(.semibold)
            if let user = userVM.currentUser {
                Text("Usuario: \(user.fullName)")
            }
            Spacer()
        }
        .padding()
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}

