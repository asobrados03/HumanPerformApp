//
//  DocumentView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI

/// Pantalla para la gestión de documentos del usuario.
struct DocumentView: View {
    @EnvironmentObject var userVM: UserViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Documento")
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

