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
    @EnvironmentObject var sessionVM: shared.UserSessionViewModel

    var body: some View {
        Group {
            if UITestConfig.isMockNetworkEnabled {
                List {
                    Section(header: Text("Información Personal")) {
                        ProfileRow(label: "Nombre completo", value: "Usuario Mock")
                        ProfileRow(label: "Correo electrónico", value: "mock@humanperform.app")
                    }
                }
                .listStyle(.insetGrouped)
                .accessibilityIdentifier("myProfileLoadedMarker")
            } else {
            if sessionVM.isLoading {
                ProgressView()
            } else if let user = sessionVM.userData {
                List {
                    Section(header: Text("Información Personal")) {
                        ProfileRow(label: "Nombre completo", value: user.fullName)
                        ProfileRow(label: "Correo electrónico", value: user.email)
                        ProfileRow(label: "Teléfono", value: user.phone)
                        ProfileRow(label: "Sexo", value: localizedSex(user.sex))
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
        }
        .navigationTitle("Mi perfil")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .accessibilityIdentifier("myProfileView")
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

private func localizedSex(_ sex: String?) -> String {
    switch sex {
    case "Male": return "Hombre"
    case "Female": return "Mujer"
    default: return sex ?? ""
    }
}
