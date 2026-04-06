import SwiftUI
import shared
import KMPObservableViewModelSwiftUI
import PhotosUI

// MARK: - Modelos equivalentes

struct SexOption: Identifiable, Hashable {
    let id = UUID()
    let label: String
    let backendValue: String
    let systemImage: String
}


enum RegisterField: String, CaseIterable, Hashable {
    case firstName, lastName, email, phone, password, dateOfBirth, sex, postcode, postalAddress, dni
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

/// Valida formato exacto de máscara dd/MM/yyyy.
func hasExactDateMaskFormat(_ s: String) -> Bool {
    guard s.count == 10 else { return false }
    let chars = Array(s)
    return chars[0].isNumber &&
        chars[1].isNumber &&
        chars[2] == "/" &&
        chars[3].isNumber &&
        chars[4].isNumber &&
        chars[5] == "/" &&
        chars[6].isNumber &&
        chars[7].isNumber &&
        chars[8].isNumber &&
        chars[9].isNumber
}

// MARK: - Vista principal

struct RegisterView: View {
    // Navegación externa
    var onNavigateToLogin: () -> Void = {}
    
    // Environment para dismiss
    @Environment(\.dismiss) private var dismiss

    // VM
    @StateViewModel private var vm = SharedDependencies.shared.makeAuthViewModel()
    
    // Inicializador por defecto (sin parámetros)
    init(onNavigateToLogin: @escaping () -> Void = {}) {
        self.onNavigateToLogin = onNavigateToLogin
    }

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
    @State private var selectedSexBackend = "" // "Male" | "Female" | ""

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
        ScrollView {
            VStack(spacing: 12) {
                headerSection
                profileImageSection
                personalInfoFields
                contactFields
                securityFields
                addressFields
                termsSection
                errorSection
                loadingSection
                registerButton
                loginLink
            }
            .padding(.horizontal, 16)
        }
         .navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier("registerView") // importante para centrar el contenido del .principal
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo() // o NavBarLogo(name: "otro_asset", height: 24)
            }
        }
        .onChange(of: vm.registerState, perform: handleStateChange)
        .confirmationDialog("Foto de perfil", isPresented: $showPhotoChooser, titleVisibility: .visible) {
            photoDialogButtons
        }
        .sheet(isPresented: $showCamera) {
            CameraView(image: $profileImage, fileName: $profilePicName, fileData: $profilePicBytes)
        }
        .alert("Te has registrado exitosamente", isPresented: $showSuccessAlert) {
            Button("OK", role: .cancel) {
                onNavigateToLogin()
            }
        }
    }

    // MARK: - View Components

    private var headerSection: some View {
        VStack(spacing: 12) {
            Text("Registro")
                .font(.title)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var profileImageSection: some View {
        VStack(spacing: 8) {
            ZStack {
                if let img = profileImage {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 90, height: 90)
                        .clipShape(Circle())
                } else {
                    Circle()
                        .fill(Color.secondary.opacity(0.15))
                        .frame(width: 90, height: 90)
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
    }

    private var personalInfoFields: some View {
        VStack(spacing: 12) {
            TextFieldIcon("Nombre", text: $firstName, systemImage: "person")
            FieldError(field: .firstName, errors: fieldErrors)
            
            TextFieldIcon("Apellidos", text: $lastName, systemImage: "person")
            FieldError(field: .lastName, errors: fieldErrors)
        }
    }

    private var contactFields: some View {
        VStack(spacing: 12) {
            TextFieldIcon("Correo electrónico", text: $email, systemImage: "envelope", keyboard: .emailAddress, textContentType: .emailAddress)
            FieldError(field: .email, errors: fieldErrors)
            
            TextFieldIcon("Teléfono", text: $phone, systemImage: "phone", keyboard: .phonePad, textContentType: .telephoneNumber)
            FieldError(field: .phone, errors: fieldErrors)
        }
    }

    private var securityFields: some View {
        VStack(spacing: 12) {
            SecureFieldIcon(placeholder: "Contraseña", text: $password, isVisible: $passwordVisible)
            FieldError(field: .password, errors: fieldErrors)
            
            // Sexo (Picker estilo menú)
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Image(systemName: "person.2").foregroundStyle(.secondary)
                    Picker("Sexo", selection: $selectedSexBackend) {
                        Text("Sexo").tag("")
                        ForEach(sexOptions, id: \.backendValue) { opt in
                            Text(opt.label).tag(opt.backendValue)
                        }
                    }
                    .pickerStyle(.menu)
                    Spacer()
                }
                .padding(12)
                .background(RoundedRectangle(cornerRadius: 12).strokeBorder(.quaternary))
                FieldError(field: .sex, errors: fieldErrors)
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
                FieldError(field: .dateOfBirth, errors: fieldErrors)
            }
        }
    }

    private var addressFields: some View {
        VStack(spacing: 12) {
            TextFieldIcon("Dirección Postal", text: $postalAddress, systemImage: "mappin.and.ellipse")
            FieldError(field: .postalAddress, errors: fieldErrors)
            
            TextFieldIcon("Código Postal", text: $postalCode, systemImage: "building.2", keyboard: .numberPad)
            FieldError(field: .postcode, errors: fieldErrors)
            
            TextFieldIcon("DNI", text: $dni, systemImage: "person.text.rectangle")
            FieldError(field: .dni, errors: fieldErrors)
        }
    }

    private var termsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 12) {
                Toggle(isOn: $acceptTerms) { Text("Acepto") }
                    .labelsHidden()
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
    }

    private var errorSection: some View {
        Group {
            if let msg = errorMessage {
                Text(msg)
                    .foregroundStyle(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
    }

    private var loadingSection: some View {
        Group {
            if isLoadingState(vm.registerState) {
                ProgressView().padding(.vertical, 8)
            } else {
                EmptyView()
            }
        }
    }

    private var registerButton: some View {
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
        .disabled(isLoadingState(vm.registerState))
    }

    private var loginLink: some View {
        Button("¿Ya tienes una cuenta? Inicia sesión", action: onNavigateToLogin)
            .padding(.bottom, 24)
    }

    private var photoDialogButtons: some View {
        Group {
            Button("Cámara") { showCamera = true }
            Button("Galería") { pickFromGallery() }
            if profileImage != nil {
                Button("Eliminar", role: .destructive) { removePhoto() }
            }
            Button("Cancelar", role: .cancel) { }
        }
    }

    // MARK: - Actions

    private func handleStateChange(_ newState: shared.RegisterState) {
        print("🟡 RegisterState changed to: \(newState)")

        if isIdleState(newState) {
            print("🟡 State: idle")
            return
        }

        if isLoadingState(newState) {
            print("🟡 State: loading")
            return
        }

        if isValidationErrorsState(newState) {
            let rawErrors = propertyValue(named: "fieldErrors", from: newState)
            print("🟡 State: validationErrors - \(String(describing: rawErrors))")
            fieldErrors = mapValidationErrors(rawErrors as Any)
            return
        }

        if isErrorState(newState) {
            let rawMessage = propertyValue(named: "message", from: newState) as? String
            print("🟡 State: error - \(rawMessage ?? "")")
            errorMessage = rawMessage
            return
        }

        if isSuccessState(newState) {
            print("🟡 State: success")
            clearForm()
            showSuccessAlert = true
        }
    }

    private func onRegister() {
        print("🔵 onRegister() called")
        
        // Validaciones locales simples
        guard acceptTerms else {
            print("❌ Terms not accepted")
            errorMessage = "Debes aceptar los términos y condiciones"
            return
        }
        guard acceptPrivacy else {
            print("❌ Privacy not accepted")
            errorMessage = "Debes aceptar la política de privacidad"
            return
        }
        guard hasExactDateMaskFormat(dobText) else {
            print("❌ Invalid dateOfBirth format: \(dobText)")
            fieldErrors[.dateOfBirth] = "La fecha debe tener formato dd/MM/yyyy"
            errorMessage = "Revisa la fecha de nacimiento"
            return
        }
        
        print("✅ Validations passed")
        errorMessage = nil
        fieldErrors = [:]

        let req = RegisterRequest(
            name: firstName,
            surnames: lastName,
            email: email,
            phone: phone,
            password: password,
            sex: selectedSexBackend,
            dateOfBirth: dobText,
            postCode: postalCode,
            postAddress: postalAddress,
            dni: dni,
            // API contract uses lowercase identifiers for `device_type` (e.g., "android", "ios").
            deviceType: "ios",
            profilePicBytes: profilePicBytes?.asKotlinByteArray(),
            profilePicName: profilePicName
        )
        
        print("🔵 Register payload (password omitted):")
        print("  - name: \(req.name)")
        print("  - surnames: \(req.surnames)")
        print("  - email: \(req.email)")
        print("  - phone: \(req.phone)")
        print("  - sex: \(req.sex)")
        print("  - dateOfBirth: \(req.dateOfBirth)")
        print("  - postCode: \(req.postCode)")
        print("  - postAddress: \(req.postAddress)")
        print("  - dni: \(req.dni)")
        print("  - deviceType: \(req.deviceType)")
        print("  - profilePicName: \(req.profilePicName ?? "nil")")
        print("  - profilePicBytesLength: \(profilePicBytes?.count ?? 0)")
        
        vm.register(data: req)
        print("🔵 vm.register called")
    }




    private func stateTypeName(_ state: shared.RegisterState) -> String {
        String(describing: type(of: state))
    }

    private func isIdleState(_ state: shared.RegisterState) -> Bool {
        stateTypeName(state).contains("Idle")
    }

    private func isLoadingState(_ state: shared.RegisterState) -> Bool {
        stateTypeName(state).contains("Loading")
    }

    private func isValidationErrorsState(_ state: shared.RegisterState) -> Bool {
        stateTypeName(state).contains("ValidationErrors")
    }

    private func isErrorState(_ state: shared.RegisterState) -> Bool {
        stateTypeName(state).contains("Error")
    }

    private func isSuccessState(_ state: shared.RegisterState) -> Bool {
        stateTypeName(state).contains("Success")
    }

    private func propertyValue(named key: String, from state: Any) -> Any? {
        Mirror(reflecting: state).children.first { $0.label == key }?.value
    }
    private func mapValidationErrors(_ rawErrors: Any) -> [RegisterField: String] {
        var mapped: [RegisterField: String] = [:]

        guard let errors = rawErrors as? [AnyHashable: Any] else {
            return mapped
        }

        for (key, value) in errors {
            guard let message = value as? String else { continue }
            let raw = String(describing: key).uppercased()
            if raw.contains("FIRST_NAME") { mapped[.firstName] = message }
            else if raw.contains("LAST_NAME") { mapped[.lastName] = message }
            else if raw.contains("EMAIL") { mapped[.email] = message }
            else if raw.contains("PHONE") { mapped[.phone] = message }
            else if raw.contains("PASSWORD") { mapped[.password] = message }
            else if raw.contains("DATE_OF_BIRTH") { mapped[.dateOfBirth] = message }
            else if raw.contains("SEX") { mapped[.sex] = message }
            else if raw.contains("POSTCODE") { mapped[.postcode] = message }
            else if raw.contains("POSTAL_ADDRESS") { mapped[.postalAddress] = message }
            else if raw.contains("DNI") { mapped[.dni] = message }
        }
        return mapped
    }

    private func clearForm() {
        firstName = ""; lastName = ""; email = ""; phone = ""
        password = ""; dobText = ""; postalCode = ""; postalAddress = ""; dni = ""
        selectedSexBackend = ""; acceptTerms = false; acceptPrivacy = false
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
        Task {
            if let result = try? await ImagePickerHelper.pickFromLibrary() {
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
                .textContentType(textContentType)
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

@MainActor
enum ImagePickerHelper {
    struct Result {
        var image: UIImage
        var data: Data
        var suggestedName: String
    }

    static func pickFromLibrary() async throws -> Result {
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

            guard let root = UIApplication.shared.connectedScenes
                .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
                .first?.rootViewController else {
                continuation.resume(throwing: CancellationError()); return
            }
            let del = Delegate(continuation)
            picker.delegate = del

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
