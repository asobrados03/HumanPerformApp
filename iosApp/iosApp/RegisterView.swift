import SwiftUI
import PhotosUI
import UniformTypeIdentifiers

// MARK: - Modelos equivalentes

struct SexOption: Identifiable, Hashable {
    let id = UUID()
    let label: String
    let backendValue: String
    let systemImage: String
}

struct RegisterRequest {
    var firstName: String
    var lastName: String
    var email: String
    var phone: String
    var password: String
    var sex: String
    var dateOfBirthText: String
    var postalCode: String
    var postalAddress: String
    var dni: String
    var profilePicBytes: Data?
    var profilePicName: String?
}

enum RegisterField: String, CaseIterable, Hashable {
    case FIRST_NAME, LAST_NAME, EMAIL, PHONE, PASSWORD, DATE_OF_BIRTH, SEX, POSTCODE, POSTAL_ADDRESS, DNI
}

enum RegisterState: Equatable {
    case idle
    case loading
    case success
    case error(message: String)
    case validationErrors([RegisterField: String])
}

// MARK: - ViewModel (simulación de AuthViewModel)

final class AuthViewModel: ObservableObject {
    @Published var registerState: RegisterState = .idle

    func resetStates() {
        registerState = .idle
    }

    func register(_ req: RegisterRequest) {
        // Simula validaciones del backend
        var errs: [RegisterField:String] = [:]
        if req.firstName.isEmpty { errs[.FIRST_NAME] = "El nombre es obligatorio" }
        if req.lastName.isEmpty  { errs[.LAST_NAME]  = "Los apellidos son obligatorios" }
        if !req.email.contains("@"){ errs[.EMAIL]   = "Correo inválido" }
        if req.password.count < 6 { errs[.PASSWORD] = "Mínimo 6 caracteres" }
        if req.sex.isEmpty       { errs[.SEX]       = "Selecciona sexo" }
        if !errs.isEmpty {
            registerState = .validationErrors(errs)
            return
        }
        registerState = .loading
        // Simula llamada de red
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.registerState = .success
        }
    }
}

// MARK: - Helpers

/// Filtro sencillo para fecha dd/MM/yyyy
func filterDateInput(_ s: String) -> String {
    let allowed = s.filter { $0.isNumber }
    var out = ""
    for (i, ch) in allowed.prefix(8).enumerated() {
        if i == 2 || i == 4 { out.append("/") }
        out.append(ch)
    }
    return out
}

// MARK: - Vista principal

struct RegisterView: View {
    // Navegación externa
    var onNavigateToLogin: () -> Void = {}

    // VM
    @StateObject private var vm = AuthViewModel()

    // Campos
    @State private var firstName = ""
    @State private var lastName  = ""
    @State private var email     = ""
    @State private var phone     = ""
    @State private var password  = ""
    @State private var passwordVisible = false

    @State private var dobText   = ""
    @State private var postalCode = ""
    @State private var postalAddress = ""
    @State private var dni = ""

    // Errores de campo
    @State private var fieldErrors: [RegisterField:String] = [:]
    @State private var errorMessage: String?

    // Sexo
    private let sexOptions = [
        SexOption(label: "Masculino", backendValue: "Male",   systemImage: "figure.male"),
        SexOption(label: "Femenino",  backendValue: "Female", systemImage: "figure.female")
    ]
    @State private var selectedSex: SexOption?

    // Términos
    @Environment(\.openURL) private var openURL
    @State private var acceptTerms = false
    @State private var acceptPrivacy = false

    // Imagen de perfil
    @State private var showPhotoChooser = false
    @State private var showCamera = false
    @State private var photoItem: PhotosPickerItem?
    @State private var profileImage: UIImage?
    @State private var profilePicBytes: Data?
    @State private var profilePicName: String?

    // Alertas
    @State private var showSuccessAlert = false

    var body: some View {
        if #available(iOS 17.0, *) {
            ScrollView {
                VStack(spacing: 12) {
                    // AppBar minimal
                    HStack {
                        Image("colored_logo")
                            .resizable()
                            .scaledToFit()
                            .frame(height: 36)
                        Spacer()
                    }
                    .padding(.top)
                    
                    Text("Registro")
                        .font(.title)
                        .bold()
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Imagen de perfil
                    VStack(spacing: 8) {
                        ZStack {
                            if let img = profileImage {
                                Image(uiImage: img)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 120, height: 120)
                                    .clipShape(Circle())
                            } else {
                                Circle()
                                    .fill(Color.secondary.opacity(0.15))
                                    .frame(width: 120, height: 120)
                                    .overlay(Image(systemName: "person.fill")
                                        .font(.system(size: 44)).foregroundColor(.secondary))
                            }
                        }
                        HStack(spacing: 12) {
                            Button("Cambiar foto") { showPhotoChooser = true }
                            if profileImage != nil {
                                Button("Quitar") {
                                    profileImage = nil
                                    profilePicBytes = nil
                                    profilePicName = nil
                                }
                            }
                        }
                        .font(.callout)
                    }
                    .padding(.bottom, 6)
                    
                    // Campos
                    Group {
                        TextFieldIcon("Nombre", text: $firstName, systemImage: "person")
                        FieldError(field: .FIRST_NAME, errors: fieldErrors)
                        
                        TextFieldIcon("Apellidos", text: $lastName, systemImage: "person")
                        FieldError(field: .LAST_NAME, errors: fieldErrors)
                        
                        TextFieldIcon("Correo electrónico", text: $email, systemImage: "envelope", keyboard: .emailAddress, textContentType: .emailAddress)
                        FieldError(field: .EMAIL, errors: fieldErrors)
                        
                        TextFieldIcon("Teléfono", text: $phone, systemImage: "phone", keyboard: .phonePad, textContentType: .telephoneNumber)
                        FieldError(field: .PHONE, errors: fieldErrors)
                        
                        SecureFieldIcon(placeholder: "Contraseña", text: $password, isVisible: $passwordVisible)
                        FieldError(field: .PASSWORD, errors: fieldErrors)
                        
                        // Sexo (Picker estilo menú)
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Image(systemName: selectedSex?.systemImage ?? "person.2")
                                    .foregroundStyle(.secondary)
                                Picker("Sexo", selection: Binding(
                                    get: { selectedSex ?? SexOption(label: "", backendValue: "", systemImage: "person") },
                                    set: { selectedSex = $0.backendValue.isEmpty ? nil : $0 }
                                )) {
                                    Text(selectedSex?.label ?? "Sexo")
                                        .tag(SexOption(label: "", backendValue: "", systemImage: "person"))
                                    ForEach(sexOptions) { opt in
                                        Text(opt.label).tag(opt)
                                    }
                                }
                                .pickerStyle(.menu)
                                Spacer()
                            }
                            .padding(12)
                            .background(RoundedRectangle(cornerRadius: 12).strokeBorder(.quaternary))
                            FieldError(field: .SEX, errors: fieldErrors)
                        }
                        
                        // Fecha de nacimiento (máscara simple dd/MM/yyyy)
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Image(systemName: "calendar")
                                    .foregroundStyle(.secondary)
                                TextField("Fecha de nacimiento (dd/mm/yyyy)", text: Binding(
                                    get: { dobText },
                                    set: { dobText = filterDateInput($0) }
                                ))
                                .keyboardType(.numberPad)
                            }
                            .padding(12)
                            .background(RoundedRectangle(cornerRadius: 12).strokeBorder(.quaternary))
                            FieldError(field: .DATE_OF_BIRTH, errors: fieldErrors)
                        }
                        
                        TextFieldIcon("Dirección Postal", text: $postalAddress, systemImage: "mappin.and.ellipse")
                        FieldError(field: .POSTAL_ADDRESS, errors: fieldErrors)
                        
                        TextFieldIcon("Código Postal", text: $postalCode, systemImage: "building.2", keyboard: .numberPad)
                        FieldError(field: .POSTCODE, errors: fieldErrors)
                        
                        TextFieldIcon("DNI", text: $dni, systemImage: "idbadge")
                        FieldError(field: .DNI, errors: fieldErrors)
                    }
                    
                    // Términos
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 12) {
                            Toggle(isOn: $acceptTerms) { Text("Acepto") }
                                .labelsHidden()                         // oculta la etiqueta a la izquierda
                            Button("términos y condiciones") {
                                openURL(URL(string: "https://www.humanperformcenter.com/cliente/condiciones")!)
                            }
                            .underline()
                        }
                        HStack(spacing: 12) {
                            Toggle(isOn: $acceptPrivacy) { Text("Acepto") }
                                .labelsHidden()
                            Button("política de privacidad") {
                                openURL(URL(string: "https://www.humanperformcenter.com/cliente/politica-privacidad")!)
                            }
                            .underline()
                        }
                    }
                    
                    if let msg = errorMessage {
                        Text(msg)
                            .foregroundStyle(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    
                    // Estados
                    switch vm.registerState {
                    case .loading:
                        ProgressView().padding(.vertical, 8)
                    default: EmptyView()
                    }
                    
                    // Botón registrar
                    Button(action: onRegister) {
                        Text("Registrarse")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(LinearGradient(colors: [Color(red:0x6D/255, green:0x2A/255, blue:0x6F/255),
                                                                Color(red:0xEF/255, green:0x0E/255, blue:0x29/255)],
                                                       startPoint: .leading, endPoint: .trailing))
                            .foregroundStyle(.white)
                            .clipShape(Capsule())
                    }
                    .disabled(vm.registerState == .loading)
                    
                    // Enlace login
                    Button("¿Ya tienes una cuenta? Inicia sesión", action: onNavigateToLogin)
                        .padding(.bottom, 24)
                }
                .padding(.horizontal, 16)
            }
            .navigationTitle("Registro")
            .navigationBarTitleDisplayMode(.inline)
            .onChange(of: vm.registerState) { _, newValue in
                switch newValue {
                case .validationErrors(let fe): fieldErrors = fe
                case .error(let message): errorMessage = message
                case .success:
                    vm.resetStates()
                    clearForm()
                    showSuccessAlert = true
                default: break
                }
            }
            // Selector de foto: cámara/galería
            .confirmationDialog("Foto de perfil", isPresented: $showPhotoChooser, titleVisibility: .visible) {
                Button("Cámara") { showCamera = true }
                Button("Galería") { pickFromGallery() }
                if profileImage != nil { Button("Eliminar", role: .destructive) { removePhoto() } }
                Button("Cancelar", role: .cancel) { }
            }
            .sheet(isPresented: $showCamera) {
                CameraView(image: $profileImage, fileName: $profilePicName, fileData: $profilePicBytes)
            }
            .alert("Te has registrado exitosamente", isPresented: $showSuccessAlert) {
                Button("OK", role: .cancel) { }
            }
        } else {
            // Fallback on earlier versions
        }
    }

    // MARK: - Actions

    private func onRegister() {
        // Validaciones locales simples
        guard acceptTerms else { errorMessage = "Debes aceptar los términos y condiciones"; return }
        guard acceptPrivacy else { errorMessage = "Debes aceptar la política de privacidad"; return }
        errorMessage = nil
        fieldErrors = [:]

        let req = RegisterRequest(
            firstName: firstName,
            lastName: lastName,
            email: email,
            phone: phone,
            password: password,
            sex: selectedSex?.backendValue ?? "",
            dateOfBirthText: dobText,
            postalCode: postalCode,
            postalAddress: postalAddress,
            dni: dni,
            profilePicBytes: profilePicBytes,
            profilePicName: profilePicName
        )
        vm.register(req)
    }

    private func clearForm() {
        firstName = ""; lastName = ""; email = ""; phone = ""
        password = ""; dobText = ""; postalCode = ""; postalAddress = ""; dni = ""
        selectedSex = nil; acceptTerms = false; acceptPrivacy = false
        removePhoto()
        errorMessage = nil
        fieldErrors = [:]
    }

    private func removePhoto() {
        profileImage = nil
        profilePicBytes = nil
        profilePicName = nil
    }

    private func pickFromGallery() {
        // Usamos PhotosPicker (iOS 16+). Alternativa: PHPickerViewController
        // Presentación inline para simplificar:
        Task {
            if let result = try? await PhotosPicker.pick(source: .photoLibrary) {
                await MainActor.run {
                    profileImage = result.image
                    profilePicBytes = result.data
                    profilePicName = result.suggestedName
                }
            }
        }
    }
}

// MARK: - Subviews reutilizables

struct TextFieldIcon: View {
    var placeholder: String
    @Binding var text: String
    var systemImage: String
    var keyboard: UIKeyboardType = .default
    var textContentType: UITextContentType? = nil

    init(_ placeholder: String, text: Binding<String>, systemImage: String, keyboard: UIKeyboardType = .default, textContentType: UITextContentType? = nil) {
        self.placeholder = placeholder
        self._text = text
        self.systemImage = systemImage
        self.keyboard = keyboard
        self.textContentType = textContentType
    }

    var body: some View {
        HStack {
            Image(systemName: systemImage).foregroundStyle(.secondary)
            TextField(placeholder, text: $text)
                .keyboardType(keyboard)
                .textContentType(textContentType)    // ✅ sin map, sin SwiftUI.TextContentType
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 12).strokeBorder(.quaternary))
    }
}

struct SecureFieldIcon: View {
    var placeholder: String
    @Binding var text: String
    @Binding var isVisible: Bool

    var body: some View {
        HStack {
            Image(systemName: "lock").foregroundStyle(.secondary)
            if isVisible {
                TextField(placeholder, text: $text)
                    .textContentType(.password)
                    .autocapitalization(.none)
                    .textInputAutocapitalization(.never)
            } else {
                SecureField(placeholder, text: $text)
                    .textContentType(.password)
            }
            Button(action: { isVisible.toggle() }) {
                Image(systemName: isVisible ? "eye.slash" : "eye")
            }
            .tint(.secondary)
        }
        .padding(12)
        .background(RoundedRectangle(cornerRadius: 12).strokeBorder(.quaternary))
    }
}

struct FieldError: View {
    let field: RegisterField
    let errors: [RegisterField:String]
    var body: some View {
        if let msg = errors[field], !msg.isEmpty {
            Text(msg).font(.caption).foregroundStyle(.red).frame(maxWidth: .infinity, alignment: .leading)
        } else { EmptyView() }
    }
}

// MARK: - Cámara (UIImagePickerController wrapper)

struct CameraView: UIViewControllerRepresentable {
    @Environment(\.dismiss) private var dismiss
    @Binding var image: UIImage?
    @Binding var fileName: String?
    @Binding var fileData: Data?

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    final class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: CameraView
        init(_ parent: CameraView) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let uiImage = info[.originalImage] as? UIImage {
                parent.image = uiImage
                let data = uiImage.jpegData(compressionQuality: 0.9)
                parent.fileData = data
                parent.fileName = "IMG_\(Int(Date().timeIntervalSince1970)).jpg"
            }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}

// MARK: - PhotosPicker helpers (galería)

@available(iOS 16.0, *)
extension PhotosPicker {
    static func pick(source: PHPickerConfiguration.AssetRepresentationMode) async throws -> (image: UIImage, data: Data, suggestedName: String) {
        // Presentación modal simplificada no está disponible fuera de un View.
        // Para mantenerlo simple, creamos un picker independiente que la vista llama vía RegisterView.pickFromGallery()
        throw CancellationError()
    }
}

// Utilidad simple para usar PhotosPicker inline en Task (sin UI custom compleja)
@MainActor
enum PhotosPicker {
    struct Result {
        var image: UIImage
        var data: Data
        var suggestedName: String
    }

    static func pick(source: UIImagePickerController.SourceType = .photoLibrary) async throws -> Result {
        // Usamos un helper controlador temporal
        return try await withCheckedThrowingContinuation { continuation in
            let picker = UIImagePickerController()
            picker.sourceType = .photoLibrary
            class Delegate: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
                let cont: CheckedContinuation<Result, Error>
                init(_ c: CheckedContinuation<Result, Error>) { cont = c }
                func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
                    if let img = info[.originalImage] as? UIImage,
                       let data = img.jpegData(compressionQuality: 0.9) {
                        let name = (info[.imageURL] as? URL)?.lastPathComponent ?? "photo.jpg"
                        picker.presentingViewController?.dismiss(animated: true) {
                            self.cont.resume(returning: .init(image: img, data: data, suggestedName: name))
                        }
                    } else {
                        picker.presentingViewController?.dismiss(animated: true) {
                            self.cont.resume(throwing: CancellationError())
                        }
                    }
                }
                func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
                    picker.presentingViewController?.dismiss(animated: true) {
                        self.cont.resume(throwing: CancellationError())
                    }
                }
            }

            // Present top-most
            guard let root = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController else {
                continuation.resume(throwing: CancellationError()); return
            }
            let del = Delegate(continuation)
            picker.delegate = del

            // Retain delegate
            objc_setAssociatedObject(picker, "picker_delegate", del, .OBJC_ASSOCIATION_RETAIN_NONATOMIC)
            root.present(picker, animated: true)
        }
    }
}

// MARK: - Preview

struct RegisterView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationStack {
            RegisterView()
        }
    }
}
