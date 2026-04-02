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
                        imageFileName = name ?? "IMG_\(Int(Date().timeIntervalSince1970)).jpg"
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

    private var profileStateKey: String {
        switch profileVM.updateState {
        case is UpdateStateIdle:
            return "idle"
        case is UpdateStateLoading:
            return "loading"
        case is UpdateStateSuccess:
            return "success"
        case let error as UpdateStateError:
            return "error:\(error.message)"
        case let validation as UpdateStateValidationErrors:
            return "validation:\(validation.fieldErrors.description)"
        default:
            return "unknown"
        }
    }

    private var deletePhotoStateKey: String {
        switch profileVM.deleteProfilePicState {
        case is DeleteProfilePicStateIdle:
            return "idle"
        case is DeleteProfilePicStateLoading:
            return "loading"
        case is DeleteProfilePicStateSuccess:
            return "success"
        case let error as DeleteProfilePicStateError:
            return "error:\(error.message)"
        default:
            return "unknown"
        }
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
        profileVM.updateUser(candidate: updated, profilePicBytes: imageData?.asKotlinByteArray(), currentUser: sessionVM.currentUserState())
    }

    private func deletePhoto(user: User) {
        profileVM.deleteProfilePic(user: user, currentUser: sessionVM.currentUserState())
    }

    private func handleProfileStateChange() {
        switch profileVM.updateState {
        case is UpdateStateLoading:
            isSaving = true
        case is UpdateStateSuccess:
            isSaving = false
            alertMessage = "Perfil actualizado"
            pendingDismiss = true
            showAlert = true
            profileVM.clearUpdateState()
        case let error as UpdateStateError:
            isSaving = false
            alertMessage = error.message
            showAlert = true
            profileVM.clearUpdateState()
        case let validation as UpdateStateValidationErrors:
            isSaving = false
            alertMessage = validation.fieldErrors.values.joined(separator: "\n")
            showAlert = true
            profileVM.clearUpdateState()
        default:
            break
        }
    }

    private func handleDeletePhotoStateChange() {
        switch profileVM.deleteProfilePicState {
        case is DeleteProfilePicStateSuccess:
            alertMessage = "Foto eliminada"
            showAlert = true
            image = nil
            imageFileName = nil
            imageData = nil
            profileVM.clearDeleteProfilePicState()
        case let error as DeleteProfilePicStateError:
            alertMessage = error.message
            showAlert = true
            profileVM.clearDeleteProfilePicState()
        default:
            break
        }
    }
}
