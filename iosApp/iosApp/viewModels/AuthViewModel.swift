import SwiftUI
import shared

// Enum para identificar campos del formulario de login (para manejar errores de validación)
enum LoginField: String {
    case email = "EMAIL"
    case password = "PASSWORD"
}

// Estados posibles para el proceso de restablecer contraseña
enum ResetPasswordState: Equatable {
    case idle
    case loading
    case success(message: String)
    case error(message: String)
}

// Estados para el cambio de contraseña
enum ChangePasswordState: Equatable {
    case idle
    case loading
    case success(message: String)
    case error(message: String)
}

class AuthViewModel: ObservableObject {
    // Estado del registro (ya existente)
    @Published var registerState: RegisterState = .idle

    // [1] Propiedades @Published para el formulario de login
    @Published var loginEmail: String = "" {
        didSet {
            // Limpiar errores de validación si el usuario modifica el campo
            clearLoginErrorsIfNeeded()
        }
    }
    @Published var loginPassword: String = "" {
        didSet {
            // Limpiar errores de validación si el usuario modifica el campo
            clearLoginErrorsIfNeeded()
        }
    }

    // Estado del proceso de login (usado por la vista para reaccionar a los cambios)
    @Published var loginState: LoginState = .idle

    // Estado del proceso de restablecimiento de contraseña
    @Published var resetPasswordState: ResetPasswordState = .idle

    // Estado del proceso de cambio de contraseña
    @Published var changePasswordState: ChangePasswordState = .idle

    private let authUseCase = AuthUseCase(authRepository: AuthRepositoryImpl())

    // Método de registro existente (no modificado en esta extensión, se muestra para contexto)
    func register(_ form: RegisterFormData) {
        print("🟢 AuthViewModel.register() called")
        // 1) Validación compartida de los campos de registro
        print("🟢 Starting validation...")
        let validationResult = UserValidator().validateRegister(
            firstName: form.firstName,
            lastName: form.lastName,
            email: form.email,
            phone: form.phone,
            password: form.password,
            dateOfBirthText: form.dateOfBirthText,
            selectedSexBackend: form.sex,
            postcode: form.postalCode,
            address: form.postalAddress,
            dni: form.dni
        )
        print("🟢 Validation result: \(validationResult)")

        if let errorResult = validationResult as? RegisterValidationResult.Error {
            print("🔴 Validation failed: \(errorResult.fieldErrors)")
            var fieldErrorMap: [RegisterField: String] = [:]
            for (k, v) in errorResult.fieldErrors {
                let key = "\(k)"
                if let f = RegisterField(rawValue: key) {
                    fieldErrorMap[f] = v as String
                }
            }
            // Publicar errores de validación en el estado de registro
            DispatchQueue.main.async {
                self.registerState = .validationErrors(fieldErrorMap)
            }
            return
        }

        print("🟢 Validation passed, making API call...")
        // 2) Llamada real al caso de uso de registro (KMM)
        DispatchQueue.main.async { self.registerState = .loading }
        let kmmRequest = makeKMMRegisterRequest(from: form)
        print("🟢 KMM Request created, calling authUseCase.register...")
        authUseCase.register(data: kmmRequest) { result, error in
            print("🟢 API Response received")
            print("  - result: \(String(describing: result))")
            print("  - error: \(String(describing: error))")

            if let error = error {
                print("🔴 API Error: \(error.localizedDescription)")
                DispatchQueue.main.async {
                    self.registerState = .error(message: error.localizedDescription)
                }
            } else {
                print("🟢 API Success")
                DispatchQueue.main.async {
                    self.registerState = .success
                }
            }
        }
    }

    // Método auxiliar existente para convertir RegisterFormData a la petición KMM (registro)
    private func makeKMMRegisterRequest(from form: RegisterFormData) -> shared.RegisterRequest {
        let photoBytes: KotlinByteArray? = form.profilePicBytes.map { dataToKotlinByteArray($0) }
        // ✅ Labels en español tal como los espera KMM para el registro
        return shared.RegisterRequest(
            nombre:       form.firstName,
            apellidos:    form.lastName,
            email:        form.email,
            telefono:     form.phone,
            password:     form.password,
            sexo:         form.sex,
            fechaNacimiento: form.dateOfBirthText,   // Formato ddMMyyyy
            codigoPostal:   form.postalCode,
            direccionPostal: form.postalAddress,
            dni:           form.dni,
            profilePicBytes: photoBytes,
            profilePicName:  form.profilePicName
        )
    }

    // ✅ Conversión auxiliar de Data (Swift) a KotlinByteArray
    private func dataToKotlinByteArray(_ data: Data) -> KotlinByteArray {
        let bytes = [UInt8](data)
        let kba = KotlinByteArray(size: Int32(bytes.count))
        for (i, b) in bytes.enumerated() {
            kba.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return kba
    }

    // [2] Nuevo método de login que utiliza directamente las propiedades loginEmail y loginPassword
    func login() {
        print("🟢 AuthViewModel.login() called")

        // 1) Validación local
        var fieldErrors: [LoginField: String] = [:]

        let trimmedEmail = loginEmail.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedEmail.isEmpty {
            fieldErrors[.email] = "El correo electrónico es obligatorio"
        } else {
            let emailRegex = #"^[^@\s]+@[^@\s]+\.[^@\s]+$"#
            if !NSPredicate(format: "SELF MATCHES %@", emailRegex).evaluate(with: trimmedEmail) {
                fieldErrors[.email] = "Correo electrónico inválido"
            }
        }
        if loginPassword.isEmpty {
            fieldErrors[.password] = "La contraseña es obligatoria"
        }

        if !fieldErrors.isEmpty {
            DispatchQueue.main.async { self.loginState = .validationErrors(fieldErrors) }
            return
        }

        // 2) Llamada KMM
        DispatchQueue.main.async { self.loginState = .loading }
        print("🟢 Validation passed, making API call...")

        authUseCase.login(email: trimmedEmail, password: loginPassword) { result, error in
            print("🟢 API Response received")
            print("  - result: \(String(describing: result))")
            print("  - error: \(String(describing: error))")

            // Error Swift directo
            if let error = error {
                print("🔴 API Error: \(error.localizedDescription)")
                DispatchQueue.main.async {
                    self.loginState = .error(message: error.localizedDescription)
                }
                return
            }

            guard let anyResult = result else {
                print("🔴 API Error: Respuesta de login vacía")
                DispatchQueue.main.async {
                    self.loginState = .error(message: "Respuesta de login vacía")
                }
                return
            }

            // En algunos bridges de KMM, un Result.Failure llega como Any cuya descripción contiene "Failure("
            let desc = String(describing: anyResult)
            if desc.contains("Failure(") {
                let rawMsg: String = {
                    if let start = desc.range(of: "Failure(")?.upperBound,
                       let end = desc.lastIndex(of: ")") {
                        return String(desc[start..<end])
                    }
                    return desc
                }()
                let msg = rawMsg.contains("lateinit property prefs has not been initialized")
                    ? "Almacenamiento seguro no inicializado. Inicializa SecureStorage/DataStore en iOS antes del login."
                    : rawMsg

                print("🔴 API Failure: \(msg)")
                DispatchQueue.main.async {
                    self.loginState = .error(message: msg)
                }
                return
            }

            // Éxito (no necesitamos payload aquí)
            print("🟢 API Success")
            DispatchQueue.main.async {
                self.loginState = .success
            }
        }
    }

    // Restablecer contraseña a partir de un correo
    func resetPassword(email: String) {
        DispatchQueue.main.async { self.resetPasswordState = .loading }

        authUseCase.resetPassword(email: email) { result, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.resetPasswordState = .error(message: error.localizedDescription)
                }
                return
            }

            guard let anyResult = result else {
                DispatchQueue.main.async {
                    self.resetPasswordState = .error(message: "Respuesta de restablecimiento vacía")
                }
                return
            }

            let desc = String(describing: anyResult)
            if desc.contains("Failure(") {
                let rawMsg: String = {
                    if let start = desc.range(of: "Failure(")?.upperBound,
                       let end = desc.lastIndex(of: ")") {
                        return String(desc[start..<end])
                    }
                    return desc
                }()
                DispatchQueue.main.async {
                    self.resetPasswordState = .error(message: rawMsg)
                }
                return
            }

            DispatchQueue.main.async {
                self.resetPasswordState = .success(message: "Contraseña restablecida exitosamente")
            }
        }
    }

    // Cambia la contraseña del usuario
    func changePassword(current: String, new: String, confirm: String, userId: Int32) {
        DispatchQueue.main.async { self.changePasswordState = .loading }
        authUseCase.changePassword(
            currentPassword: current,
            newPassword: new,
            confirmPassword: confirm,
            userId: userId
        ) { result, error in
            DispatchQueue.main.async {
                if let error = error {
                    self.changePasswordState = .error(message: error.localizedDescription)
                    return
                }

                guard let anyResult = result else {
                    self.changePasswordState = .error(message: "Respuesta de cambio vacía")
                    return
                }

                let desc = String(describing: anyResult)
                if desc.contains("Failure(") {
                    let userMsg = self.extractHumanMessage(from: desc)
                    self.changePasswordState = .error(message: userMsg)
                    return
                }

                self.changePasswordState = .success(message: "Contraseña cambiada correctamente")
            }
        }
    }
    
    /// Extrae un mensaje legible desde un `Result.Failure(...)` de KMM.
    /// Devuelve el texto después del último ":" o, si no hay, intenta
    /// limpiar el nombre de clase totalmente cualificado.
    private func extractHumanMessage(from failureDesc: String) -> String {
        // 1) Saca el interior de Failure( ... )
        let inner: String = {
            if let start = failureDesc.range(of: "Failure(")?.upperBound,
               let end = failureDesc.range(of: ")", options: .backwards)?.lowerBound {
                return String(failureDesc[start..<end])
            }
            return failureDesc
        }()

        // 2) Quédate con lo que hay después del último ":" (mensaje humano)
        if let lastColon = inner.lastIndex(of: ":") {
            let msg = inner[inner.index(after: lastColon)...].trimmingCharacters(in: .whitespacesAndNewlines)
            if !msg.isEmpty { return msg }
        }

        // 3) Fallback: si no hay ":", elimina el paquete y deja solo el identificador final
        let cleaned = inner.split(separator: ".").last.map(String.init) ?? inner
        return cleaned.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    // Reinicia el estado de restablecimiento de contraseña
    func resetResetPasswordState() {
        resetPasswordState = .idle
    }

    // Reinicia el estado de cambio de contraseña
    func resetChangePasswordState() {
        changePasswordState = .idle
    }


    // [3] Método para limpiar errores de login si los campos cambian y había errores de validación
    private func clearLoginErrorsIfNeeded() {
        // Si actualmente hay errores de validación mostrados, al modificar un campo se limpian
        if case .validationErrors = loginState {
            loginState = .idle
        }
    }

    // [4] Resetear todos los estados y campos (registro y login) a sus valores iniciales
    func resetStates() {
        // Restablecer estado de registro a idle
        registerState = .idle
        // Limpiar campos de formulario de login
        loginEmail = ""
        loginPassword = ""
        // Restablecer estado de login a idle
        loginState = .idle
        resetPasswordState = .idle
    }

    // [5] Conversión a tipo KMM para la solicitud de login (si fuera necesario enviar un objeto en lugar de Strings)
    // En este caso la API KMM de login toma directamente email y password,
    // pero definimos esta función para mantener un patrón similar al registro.
    private func makeKMMLoginRequest() -> [String: String] {
        // Construir un diccionario con las credenciales de login
        return ["email": loginEmail, "password": loginPassword]
    }
}
