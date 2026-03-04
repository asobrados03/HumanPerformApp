import SwiftUI
import Foundation
import shared

/// Permite editar campos del perfil de usuario y la foto.
struct EditProfileView: View {
    @EnvironmentObject var vm: shared.UserViewModel
    
    @Environment(\.dismiss) private var dismiss
    @State private var pendingDismiss = false

    @State private var email: String = ""
    @State private var fullName: String = ""
    @State private var dateOfBirth: String = ""
    @State private var selectedSexIndex: Int = -1
    @State private var phone: String = ""
    @State private var postAddress: String = ""
    @State private var postcode: String = ""
    @State private var dni: String = ""

    @State private var image: UIImage? = nil
    @State private var imageData: Data? = nil
    @State private var showPicker = false
    @State private var pickerSource: UIImagePickerController.SourceType = .photoLibrary
    @State private var showDialog = false
    @State private var imageFileName: String? = nil

    @State private var isSaving = false
    @State private var showAlert = false
    @State private var alertMessage = ""

    private let sexOptions = [("Masculino","Male"),("Femenino","Female")]

    var body: some View {
        Group {
            if let user = vm.currentUser {
                Form {
                    Section {
                        HStack {
                            Spacer()
                            EditableUserProfileImageView(photoName: user.profilePictureName, image: image) {
                                showDialog = true
                            }
                            Spacer()
                        }
                    }

                    Section(header: Text("Datos personales")) {
                        TextField("Correo", text: $email)
                            .disabled(true)
                        TextField("Nombre completo", text: $fullName)
                        TextField("Fecha de nacimiento (dd/MM/yyyy)", text: $dateOfBirth)
                            .keyboardType(.numbersAndPunctuation)
                        Picker("Sexo", selection: $selectedSexIndex) {
                            Text("").tag(-1)
                            ForEach(sexOptions.indices, id: \.self) { i in
                                Text(sexOptions[i].0).tag(i)
                            }
                        }
                        TextField("Teléfono", text: $phone)
                            .keyboardType(.phonePad)
                        TextField("Dirección postal", text: $postAddress)
                        TextField("Código postal", text: $postcode)
                            .keyboardType(.numberPad)
                        TextField("DNI", text: $dni)
                    }

                    Section {
                        Button(action: { save(user: user) }) {
                            if isSaving {
                                ProgressView()
                            } else {
                                Text("Guardar").frame(maxWidth: .infinity)
                            }
                        }
                    }
                }
                .onAppear {
                    email = user.email
                    fullName = user.fullName
                    phone = user.phone
                    postAddress = user.postAddress
                    if let pc = user.postcode { postcode = String(pc.int32Value) } else { postcode = "" }
                    dni = user.dni ?? ""
                    let parts = user.dateOfBirth.split(separator: "-")
                    if parts.count == 3 { dateOfBirth = "\(parts[2])/\(parts[1])/\(parts[0])" }
                    let idx = sexOptions.firstIndex { $0.1.lowercased() == user.sex.lowercased() }
                    selectedSexIndex = idx ?? -1
                }
                .alert(alertMessage, isPresented: $showAlert) {
                    Button("OK", role: .cancel) {
                        if pendingDismiss {
                            pendingDismiss = false
                            dismiss()          // pop tras cerrar la alerta
                        }
                    }
                }
                .confirmationDialog("Foto del perfil", isPresented: $showDialog, titleVisibility: .visible) {
                    if vm.currentUser?.profilePictureName != nil || image != nil {
                        Button("Eliminar foto", role: .destructive) { deletePhoto(user: user) }
                    }
                    Button("Cámara") { pickerSource = .camera; showPicker = true }
                    Button("Galería") { pickerSource = .photoLibrary; showPicker = true }
                    Button("Cancelar", role: .cancel) { }
                }
                .sheet(isPresented: $showPicker) {
                    ImagePicker(sourceType: pickerSource) { img, name in
                        image = img
                        imageData = img.jpegData(compressionQuality: 0.8)
                        if let n = name {
                            imageFileName = n
                        } else {
                            imageFileName = "IMG_\(Int(Date().timeIntervalSince1970)).jpg"
                        }
                    }
                }
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
        let kPostcode: KotlinInt? = {
            guard !postcode.isEmpty, let v = Int32(postcode) else { return user.postcode }
            return KotlinInt(value: v)
        }()
        let parts = dateOfBirth.split(separator: "/")
        let backendDate: String = {
            if parts.count == 3 {
                let d = parts[0].padding(toLength: 2, withPad: "0", startingAt: 0)
                let m = parts[1].padding(toLength: 2, withPad: "0", startingAt: 0)
                let y = parts[2].padding(toLength: 4, withPad: "0", startingAt: 0)
                return "\(y)-\(m)-\(d)"
            } else { return user.dateOfBirth }
        }()
        let newSex = selectedSexIndex >= 0 ? sexOptions[selectedSexIndex].1 : user.sex
        var picName = user.profilePictureName
        if image != nil {
            picName = imageFileName ?? "IMG_\(Int(Date().timeIntervalSince1970)).jpg"
        }
        let updated = User(
            id: user.id,
            fullName: fullName,
            email: user.email,
            phone: phone,
            sex: newSex,
            dateOfBirth: backendDate,
            postcode: kPostcode,
            postAddress: postAddress,
            dni: dni.isEmpty ? nil : dni,
            profilePictureName: picName
        )
        vm.updateUserProfile(updated, profilePicData: imageData) { success, error in
            DispatchQueue.main.async {
                isSaving = false
                if success {
                    alertMessage = "Perfil actualizado"
                    pendingDismiss = true
                    showAlert = true
                } else {
                    alertMessage = error ?? "Error desconocido"
                    showAlert = true
                }
            }
        }
    }

    private func deletePhoto(user: User) {
        vm.deleteProfilePic(for: user) { success, error in
            alertMessage = success ? "Foto eliminada" : (error ?? "Error desconocido")
            showAlert = true
            if success {
                image = nil
                imageFileName = nil
                imageData = nil
            }
        }
    }
}
