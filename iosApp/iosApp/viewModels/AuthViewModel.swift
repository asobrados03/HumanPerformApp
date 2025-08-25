import SwiftUI
import shared

// Enum para identificar campos del formulario de login (para manejar errores de validación)
enum LoginField: String {
    case email = "EMAIL"
    case password = "PASSWORD"
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
    }

    // [5] Conversión a tipo KMM para la solicitud de login (si fuera necesario enviar un objeto en lugar de Strings)
    // En este caso la API KMM de login toma directamente email y password,
    // pero definimos esta función para mantener un patrón similar al registro.
    private func makeKMMLoginRequest() -> [String: String] {
        // Construir un diccionario con las credenciales de login
        return ["email": loginEmail, "password": loginPassword]
    }
}
