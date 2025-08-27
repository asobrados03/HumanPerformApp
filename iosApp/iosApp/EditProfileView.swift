//
//  EditProfileView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Permite editar campos básicos del perfil de usuario.
struct EditProfileView: View {
    @EnvironmentObject var vm: UserViewModel

    @State private var fullName: String = ""
    @State private var phone: String = ""
    @State private var postAddress: String = ""
    @State private var postcode: String = ""
    @State private var isSaving = false
    @State private var showAlert = false
    @State private var alertMessage = ""

    var body: some View {
        Group {
            if let user = vm.currentUser {
                Form {
                    Section(header: Text("Datos personales")) {
                        TextField("Nombre completo", text: $fullName)
                        TextField("Teléfono", text: $phone)
                        TextField("Dirección postal", text: $postAddress)
                        TextField("Código postal", text: $postcode)
                            .keyboardType(.numberPad)
                    }

                    Section {
                        Button(action: { save(user: user) }) {
                            if isSaving {
                                ProgressView()
                            } else {
                                Text("Guardar")
                                    .frame(maxWidth: .infinity)
                            }
                        }
                    }
                }
                .onAppear {
                    fullName = user.fullName
                    phone = user.phone
                    postAddress = user.postAddress
                    postcode = user.postcode.map { String($0) } ?? ""
                }
                .alert(alertMessage, isPresented: $showAlert) { Button("OK", role: .cancel) { } }
            } else {
                Text("Sin usuario")
            }
        }
        .navigationTitle("Editar perfil")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }

    private func save(user: User) {
        isSaving = true
        let updated = User(
            id: user.id,
            fullName: fullName,
            email: user.email,
            phone: phone,
            sex: user.sex,
            dateOfBirth: user.dateOfBirth,
            postcode: Int32(postcode) ?? user.postcode,
            postAddress: postAddress,
            dni: user.dni,
            profilePictureName: user.profilePictureName
        )
        vm.updateUserProfile(updated, profilePicData: nil) { success, error in
            isSaving = false
            alertMessage = success ? "Perfil actualizado" : (error ?? "Error desconocido")
            showAlert = true
        }
    }
}

