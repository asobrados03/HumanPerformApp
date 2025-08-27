//
//  MyProfileView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Muestra la información personal del usuario actual.
struct MyProfileView: View {
    @EnvironmentObject var vm: UserViewModel

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else if let user = vm.currentUser {
                List {
                    Section(header: Text("Información Personal")) {
                        ProfileRow(label: "Nombre completo", value: user.fullName)
                        ProfileRow(label: "Correo electrónico", value: user.email)
                        ProfileRow(label: "Teléfono", value: user.phone)
                        ProfileRow(label: "Sexo", value: user.sex)
                        ProfileRow(label: "Fecha de nacimiento", value: user.dateOfBirth)
                        if let dni = user.dni {
                            ProfileRow(label: "DNI", value: dni)
                        }
                        ProfileRow(label: "Dirección postal", value: user.postAddress)
                        if let pc = user.postcode {
                            ProfileRow(label: "Código postal", value: "\(pc)")
                        }
                    }
                }
                .listStyle(.insetGrouped)
            } else {
                Text("Sin usuario")
            }
        }
        .navigationTitle("Mi perfil")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}

private struct ProfileRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
        }
    }
}

