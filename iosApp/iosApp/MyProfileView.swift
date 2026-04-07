//
//  MyProfileView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Muestra la información personal del usuario actual.
struct MyProfileView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()

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
                        ProfileRow(label: "Fecha de nacimiento", value: normalizedDisplayDate(from: user.dateOfBirth))
                        if let dni = user.dni {
                            ProfileRow(label: "DNI", value: dni)
                        }
                        ProfileRow(label: "Dirección postal", value: user.postAddress)
                        if let pc = user.postcode {
                            ProfileRow(label: "Código postal", value: "\(pc.int32Value)")
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
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            Text(value)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.vertical, 2)
    }
}

private func normalizedDisplayDate(from rawDate: String) -> String {
    let trimmed = rawDate.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !trimmed.isEmpty else { return "" }

    if trimmed.contains("/") {
        return String(trimmed.prefix(10))
    }

    let isoDate = String(trimmed.prefix(10))
    let parts = isoDate.split(separator: "-")
    if parts.count == 3 {
        return "\(parts[2])/\(parts[1])/\(parts[0])"
    }

    return trimmed
}

private func localizedSex(_ sex: String?) -> String {
    switch sex {
    case "Male": return "Hombre"
    case "Female": return "Mujer"
    default: return sex ?? ""
    }
}
