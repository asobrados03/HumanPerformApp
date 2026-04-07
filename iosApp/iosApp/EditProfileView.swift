import SwiftUI
import KMPObservableViewModelSwiftUI
import Foundation
import shared

/// Permite editar campos del perfil de usuario y la foto.
struct EditProfileView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var profileVM = SharedDependencies.shared.makeUserProfileViewModel()

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
    @State private var imageData: Foundation.Data? = nil
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
            if let user = sessionVM.userData {
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
                        labeledTextField(label: "Correo electrónico", placeholder: "nombre@correo.com", text: $email, isDisabled: true)
                        labeledTextField(label: "Nombre y apellidos", placeholder: "Introduce tu nombre completo", text: $fullName)
                        labeledTextField(label: "Fecha de nacimiento", placeholder: "dd/MM/yyyy", text: $dateOfBirth)
                            .keyboardType(.numbersAndPunctuation)
                        Picker("Sexo", selection: $selectedSexIndex) {
                            Text("Selecciona tu sexo").tag(-1)
                            ForEach(sexOptions.indices, id: \.self) { i in
                                Text(sexOptions[i].0).tag(i)
                            }
                        }
                        labeledTextField(label: "Teléfono", placeholder: "Introduce tu teléfono", text: $phone)
                            .keyboardType(.phonePad)
                        labeledTextField(label: "Dirección postal", placeholder: "Introduce tu dirección", text: $postAddress)
                        labeledTextField(label: "Código postal", placeholder: "Introduce tu código postal", text: $postcode)
                            .keyboardType(.numberPad)
                        labeledTextField(label: "DNI", placeholder: "Introduce tu DNI", text: $dni)
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
                    dateOfBirth = normalizedDisplayDate(from: user.dateOfBirth)
                    let idx = sexOptions.firstIndex { $0.1.lowercased() == user.sex.lowercased() }
                    selectedSexIndex = idx ?? -1
                }
                .alert(alertMessage, isPresented: $showAlert) {
                    Button("OK", role: .cancel) {
                        if pendingDismiss {
                            pendingDismiss = false
                            dismiss()
                        }
                    }
                }
                .confirmationDialog("Foto del perfil", isPresented: $showDialog, titleVisibility: .visible) {
                    if sessionVM.userData?.profilePictureName != nil || image != nil {
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
                        imageFileName = normalizedJpegFileName(from: name)
                    }
                }
                .onChange(of: profileStateKey) { _ in
                    handleProfileStateChange()
                }
                .onChange(of: deletePhotoStateKey) { _ in
                    handleDeletePhotoStateChange()
                }
            } else {
                Text("Sin usuario")
            }
        }
        .navigationTitle("Editar perfil")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }


    @ViewBuilder
    private func labeledTextField(
        label: String,
        placeholder: String,
        text: Binding<String>,
        isDisabled: Bool = false
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            TextField(placeholder, text: text)
                .disabled(isDisabled)
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

    private var profileStateKey: String {
        let kind = profileVM.updateStateKind()
        if kind == "error", let message = profileVM.updateStateMessage() {
            return "error:\(message)"
        }
        if kind == "validation" {
            let fieldErrors = profileVM.updateValidationFieldErrors()
            return "validation:\(fieldErrors.description)"
        }
        return kind
    }

    private var deletePhotoStateKey: String {
        let kind = profileVM.deleteProfilePicStateKind()
        if kind == "error", let message = profileVM.deleteProfilePicStateMessage() {
            return "error:\(message)"
        }
        return kind
    }

    private func save(user: User) {
        isSaving = true
        let kPostcode: KotlinInt? = {
            guard !postcode.isEmpty, let v = Int32(postcode) else { return user.postcode }
            return KotlinInt(value: v)
        }()
        // Android envía dd/MM/yyyy al ViewModel (UserValidator valida ese formato).
        // Para mantener el comportamiento equivalente entre plataformas, iOS debe enviar el mismo formato.
        let dateForValidation = dateOfBirth.trimmingCharacters(in: .whitespacesAndNewlines)
        let newSex = selectedSexIndex >= 0 ? sexOptions[selectedSexIndex].1 : user.sex
        var picName = user.profilePictureName
        if image != nil {
            picName = normalizedJpegFileName(from: imageFileName)
        }
        let updated = User(
            id: user.id,
            fullName: fullName,
            email: user.email,
            phone: phone,
            sex: newSex,
            dateOfBirth: dateForValidation.isEmpty ? user.dateOfBirth : dateForValidation,
            postcode: kPostcode,
            postAddress: postAddress,
            dni: dni.isEmpty ? nil : dni,
            profilePictureName: picName
        )
        profileVM.updateUser(candidate: updated, profilePicBytes: imageData?.asKotlinByteArray(), currentUser: sessionVM.currentUserState())
    }

    private func normalizedJpegFileName(from original: String?) -> String {
        let timestamp = Int(Date().timeIntervalSince1970)
        let fallback = "IMG_\(timestamp).jpg"
        guard let original, !original.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return fallback
        }

        let baseName = URL(fileURLWithPath: original).deletingPathExtension().lastPathComponent
        let sanitizedBase = baseName
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: " ", with: "_")

        if sanitizedBase.isEmpty {
            return fallback
        }
        return "\(sanitizedBase).jpg"
    }

    private func deletePhoto(user: User) {
        profileVM.deleteProfilePic(user: user, currentUser: sessionVM.currentUserState())
    }

    private func handleProfileStateChange() {
        if profileVM.updateStateKind() == "loading" {
            isSaving = true
        } else if profileVM.updateStateKind() == "success" {
            isSaving = false
            alertMessage = "Perfil actualizado"
            pendingDismiss = true
            showAlert = true
            profileVM.clearUpdateState()
        } else if profileVM.updateStateKind() == "error" {
            isSaving = false
            alertMessage = profileVM.updateStateMessage() ?? "Error desconocido"
            showAlert = true
            profileVM.clearUpdateState()
        } else if profileVM.updateStateKind() == "validation" {
            isSaving = false
            let fieldErrors = profileVM.updateValidationMessages()
            alertMessage = fieldErrors.isEmpty ? "Revisa los campos del formulario" : fieldErrors.joined(separator: "\n")
            showAlert = true
        }
    }

    private func handleDeletePhotoStateChange() {
        if profileVM.deleteProfilePicStateKind() == "success" {
            alertMessage = "Foto eliminada"
            showAlert = true
            image = nil
            imageFileName = nil
            imageData = nil
            profileVM.clearDeleteProfilePicState()
        } else if profileVM.deleteProfilePicStateKind() == "error" {
            alertMessage = profileVM.deleteProfilePicStateMessage() ?? "Error desconocido"
            showAlert = true
            profileVM.clearDeleteProfilePicState()
        }
    }
}
