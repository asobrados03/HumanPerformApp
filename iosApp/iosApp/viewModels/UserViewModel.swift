//
//  UserViewModel.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import Foundation
import shared
import KMPNativeCoroutinesAsync

/// ViewModel nativo (Swift) para gestionar el usuario actual en iOS.
/// No llama a métodos inexistentes de KMM (p.ej. getStoredUser / getUserById).
final class UserViewModel: ObservableObject {

    // Estado público

    /// Usuario autenticado actualmente (nil si no hay sesión)
    @Published var currentUser: User? = nil
    @Published var isLoading: Bool = true
    /// Saldo actual del monedero virtual
    @Published var balance: Double = 0.0
    
    private var userTask: Task<Void, Never>?

    /// ID del usuario autenticado (o -1 si no existe)
    var currentUserId: Int32? { currentUser?.id }

    /// ¿Hay sesión activa?
    var isUserAuthenticated: Bool { currentUser != nil }

    /// Datos de acceso rápido
    var currentUserEmail: String? { currentUser?.email }
    var currentUserName: String?  { currentUser?.fullName }

    // MARK: - Casos de uso (compartidos KMM que sí existen)

    /// Ajusta esto a tu implementación real en KMM
    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())

    // MARK: - Init
    init(onlyFirst: Bool = false) {
        userTask?.cancel()
        userTask = Task {
            var didEmitOnce = false
            do {
                for try await user in asyncSequence(for: SecureStorage.shared.userFlow()) {
                    if onlyFirst && didEmitOnce { continue }
                    didEmitOnce = true
                    await MainActor.run {
                        self.currentUser = user        // <- ahora sí se pobla
                        self.isLoading = false
                    }
                    if onlyFirst { break }            // emular firstOrNull()
                }
            } catch is CancellationError {
                // ignore
            } catch {
                await MainActor.run { self.isLoading = false }
                print("userFlow error:", error.localizedDescription)
            }
        }
    }

    // MARK: - Acciones de sesión

    /// Establece el usuario actual (por ejemplo, tras un login exitoso)
    func setCurrentUser(_ user: User) {
        DispatchQueue.main.async { [weak self] in
            self?.currentUser = user
        }
    }

    /// Limpia la sesión del usuario actual
    func clearCurrentUser() {
        DispatchQueue.main.async { [weak self] in
            self?.currentUser = nil
        }
    }

    // MARK: - Balance

    /// Solicita al caso de uso el saldo actual del usuario.
    /// - Parameter userId: identificador del usuario en KMM.
    func loadBalance(for userId: Int32) {
        userUseCase.getEwalletBalance(userId: userId) { [weak self] result, error in
            DispatchQueue.main.async {
                self?.balance = result?.doubleValue ?? 0.0
            }
        }
    }

    // MARK: - Actualización de perfil

    /// Actualiza el perfil de usuario usando el caso de uso compartido de KMM.
    /// - Parameters:
    ///   - updatedUser: objeto `User` con los datos nuevos.
    ///   - profilePicData: bytes de la nueva foto (opcional).
    ///   - completion: (success, errorMessage)
    func updateUserProfile(
        _ updatedUser: User,
        profilePicData: Data?,
        completion: @escaping (_ success: Bool, _ error: String?) -> Void
    ) {
        let byteArray: KotlinByteArray? = profilePicData.map { Self.toKotlinByteArray($0) }

        // IMPORTANTE: la firma de `updateUser` en KMM debe existir.
        // Normalmente KMM expone las suspend functions con un completion (result, error).
        userUseCase.updateUser(user: updatedUser, profilePicBytes: byteArray) { [weak self] result, error in
            guard let self = self else {
                DispatchQueue.main.async { completion(false, "ViewModel deallocated") }
                return
            }

            // Si vino un error de KMM
            if let error = error {
                DispatchQueue.main.async { completion(false, error.localizedDescription) }
                return
            }

            // Algunos generadores KMM exponen `result` como `User?`,
            // otros como un "Result-like" bridged object.
            // Intentamos ambas rutas de forma segura.

            if let updated = result as? User {
                // Caso 1: KMM nos devolvió el User directamente
                DispatchQueue.main.async {
                    self.currentUser = updated
                    completion(true, nil)
                }
                return
            }

            // Caso 2: resultado envuelto → intentar extraer un `User`
            if let updated = Self.extractUser(from: result) {
                DispatchQueue.main.async {
                    self.currentUser = updated
                    completion(true, nil)
                }
                return
            }

            // Si llegamos aquí no pudimos interpretar el resultado
            DispatchQueue.main.async {
                completion(false, "Formato de resultado desconocido al actualizar usuario")
            }
        }
    }

    // MARK: - (Opcional) Hidratar desde almacenamiento seguro
    // Si más adelante añades en KMM un método `suspend fun getStoredUser(): User?`,
    // podrás descomentar e implementar este helper.
    /*
    func hydrateFromStoredUser(completion: @escaping (Bool) -> Void) {
        userUseCase.getStoredUser { [weak self] user, error in
            DispatchQueue.main.async {
                if let u = user {
                    self?.currentUser = u
                    completion(true)
                } else {
                    completion(false)
                }
            }
        }
    }
    */

    // MARK: - Helpers

    /// Convierte `Data` de Swift a `KotlinByteArray`
    private static func toKotlinByteArray(_ data: Data) -> KotlinByteArray {
        let bytes = [UInt8](data)
        let kba = KotlinByteArray(size: Int32(bytes.count))
        for (i, byte) in bytes.enumerated() {
            kba.set(index: Int32(i), value: Int8(bitPattern: byte))
        }
        return kba
    }

    /// Intenta extraer un `User` de un resultado bridged de KMM (en caso de envoltorio tipo `Result`)
    private static func extractUser(from result: Any?) -> User? {
        guard let result = result else { return nil }
        // Intento directo
        if let user = result as? User { return user }

        // Inspección superficial por reflexión
        let mirror = Mirror(reflecting: result)
        for child in mirror.children {
            if let user = child.value as? User { return user }
            let nested = Mirror(reflecting: child.value)
            for inner in nested.children {
                if let user = inner.value as? User { return user }
            }
        }
        return nil
    }
}
