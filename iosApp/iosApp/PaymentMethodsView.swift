//
//  PaymentMethodsView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI

/// Pantalla para visualizar los métodos de pago del usuario.
struct PaymentMethodsView: View {
    @EnvironmentObject var userVM: UserViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Ver metodos de pago")
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

