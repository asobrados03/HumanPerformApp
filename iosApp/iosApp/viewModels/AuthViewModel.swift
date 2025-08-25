import SwiftUI
import shared

class AuthViewModel: ObservableObject {
    @Published var registerState: RegisterState = .idle

    private let authUseCase = AuthUseCase(authRepository: AuthRepositoryImpl())

    func register(_ form: RegisterFormData) {
        print("🟢 AuthViewModel.register() called")
        
        // 1) Validación compartida
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
            DispatchQueue.main.async {
                self.registerState = .validationErrors(fieldErrorMap)
            }
            return
        }
        
        print("🟢 Validation passed, making API call...")
        // 2) Llamada real
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
                DispatchQueue.main.async { self.registerState = .success }
            }
        }
    }

    // ✅ Devolver el tipo totalmente calificado
    private func makeKMMRegisterRequest(from form: RegisterFormData) -> shared.RegisterRequest {
        let photoBytes: KotlinByteArray? = form.profilePicBytes.map { dataToKotlinByteArray($0) }

        // ✅ Labels en español tal como los espera KMM
        return shared.RegisterRequest(
            nombre: form.firstName,
            apellidos: form.lastName,
            email: form.email,
            telefono: form.phone,
            password: form.password,
            sexo: form.sex,
            fechaNacimiento: form.dateOfBirthText,
            codigoPostal: form.postalCode,
            direccionPostal: form.postalAddress,
            dni: form.dni,
            profilePicBytes: photoBytes,          // ✅ KotlinByteArray?
            profilePicName: form.profilePicName
        )
    }

    // ✅ Conversión Data -> KotlinByteArray (sin usar subíndices)
    private func dataToKotlinByteArray(_ data: Data) -> KotlinByteArray {
        let bytes = [UInt8](data)
        let kba = KotlinByteArray(size: Int32(bytes.count))
        for (i, b) in bytes.enumerated() {
            kba.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        return kba
    }
    
    func resetStates() {
        registerState = .idle
    }
}
