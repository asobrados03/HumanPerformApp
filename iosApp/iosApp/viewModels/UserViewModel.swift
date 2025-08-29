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
import UserNotifications

/// Estado de la subida de documentos.
enum UploadState: Equatable {
    case idle
    case loading
    case success(String)
    case error(String)
}

/// Estado de la pantalla de cupones.
struct CouponUiState {
    var code: String = ""
    var isLoading: Bool = false
    var error: String? = nil
    var currentCoupons: [Coupon] = []
}

/// ViewModel nativo (Swift) para gestionar el usuario actual en iOS.
/// No llama a métodos inexistentes de KMM (p.ej. getStoredUser / getUserById).
final class UserViewModel: ObservableObject {

    // Estado público

    /// Usuario autenticado actualmente (nil si no hay sesión)
    @Published var currentUser: User? = nil
    @Published var isLoading: Bool = true
    /// Saldo actual del monedero virtual
    @Published var balance: Double = 0.0
    /// Transacciones del monedero virtual
    @Published var ewalletTransactions: [EwalletTransaction] = []
    /// Estado de la subida de documentos
    @Published var uploadState: UploadState = .idle
    /// Estado de gestión de cupones
    @Published var couponState: CouponUiState = .init()
    
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

    /// Elimina la cuenta del usuario actual en el servidor
    func deleteUser(email: String, completion: @escaping (Bool, String?) -> Void) {
        userUseCase.deleteUser(email: email) { _, error in
            DispatchQueue.main.async {
                if let error = error {
                    completion(false, error.localizedDescription)
                } else {
                    completion(true, nil)
                }
            }
        }
    }

    // MARK: - Balance

    /// Solicita al caso de uso el saldo actual del usuario.
    /// - Parameter userId: identificador del usuario en KMM.
    private static func toDouble(_ any: Any?) -> Double? {
        if let kd = any as? KotlinDouble { return kd.doubleValue }
        if let ns = any as? NSNumber    { return ns.doubleValue }
        if let d  = any as? Double      { return d }
        if let s  = any as? String      { return Double(s) }
        return nil
    }

    func loadBalance(for userId: Int32) {
        userUseCase.getEwalletBalanceForIos(userId: userId) { [weak self] balance, error in
            DispatchQueue.main.async {
                if let balance = balance {
                    self?.balance = balance.doubleValue
                } else {
                    self?.balance = 0.0
                }
            }
        }
    }

    /// Recupera las transacciones del monedero virtual.
    func loadEwalletTransactions(for userId: Int32) {
        userUseCase.getEwalletTransactions(userId: userId) { [weak self] list, _ in
            DispatchQueue.main.async {
                self?.ewalletTransactions = list ?? []
            }
        }
    }

    // MARK: - Cupones

    /// Obtiene los cupones actuales del usuario.
    func loadUserCoupons(_ userId: Int32) {
        userUseCase.getUserCouponsForIos(userId: userId) { [weak self] list, error in
            DispatchQueue.main.async {
                if let coupons = list {
                    self?.couponState.currentCoupons = coupons
                }
                self?.couponState.error = error
            }
        }
    }

    /// Actualiza el texto del cupón.
    func onCouponCodeChanged(_ code: String) {
        DispatchQueue.main.async {
            self.couponState.code = code
            self.couponState.error = nil
        }
    }

    /// Añade el cupón al usuario y recarga la lista.
    func addCouponToUser(userId: Int32, code: String) {
        DispatchQueue.main.async {
            self.couponState.isLoading = true
            self.couponState.error = nil
        }

        userUseCase.addCouponToUserForIos(userId: userId, couponCode: code) { [weak self] success, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.couponState.isLoading = false
                if success {
                    self.couponState.code = ""
                    self.loadUserCoupons(userId)
                } else {
                    self.couponState.error = error
                }
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
        userUseCase.updateUser(user: updatedUser, profilePicBytes: byteArray) { [weak self] updated, error in
            guard let self = self else {
                DispatchQueue.main.async { completion(false, "ViewModel deallocated") }
                return
            }

            if let error = error {
                DispatchQueue.main.async { completion(false, error.localizedDescription) }
                return
            }

            if let updated = updated {
                DispatchQueue.main.async {
                    self.currentUser = updated
                    completion(true, nil)
                }
            } else {
                DispatchQueue.main.async {
                    completion(false, "No se recibió usuario actualizado")
                }
            }
        }
    }

    // MARK: - Eliminación de foto de perfil
    /// Elimina la foto de perfil del usuario en el servidor.
    func deleteProfilePic(for user: User, completion: @escaping (Bool, String?) -> Void) {
        let req = DeleteProfilePicRequest(email: user.email, profilePictureName: user.profilePictureName)
        userUseCase.deleteProfilePic(req: req) { [weak self] _, error in
            guard let self = self else {
                DispatchQueue.main.async { completion(false, "ViewModel deallocated") }
                return
            }
            if let error = error {
                DispatchQueue.main.async { completion(false, error.localizedDescription) }
            } else {
                DispatchQueue.main.async {
                    self.currentUser = User(
                        id: user.id,
                        fullName: user.fullName,
                        email: user.email,
                        phone: user.phone,
                        sex: user.sex,
                        dateOfBirth: user.dateOfBirth,
                        postcode: user.postcode,
                        postAddress: user.postAddress,
                        dni: user.dni,
                        profilePictureName: nil
                    )
                    completion(true, nil)
                }
            }
        }
    }

    // MARK: - Documentos

    /// Sube un documento al servidor.
    func uploadDocument(name: String, data: Data) {
        uploadState = .loading
        let byteArray = Self.toKotlinByteArray(data)

        Task {
            do {
                let message = try await userUseCase.uploadDocumentRaw(name: name, data: byteArray)
                await MainActor.run {
                    self.uploadState = .success(message) // o .success si tu enum no lleva mensaje
                }
            } catch {
                await MainActor.run {
                    self.uploadState = .error(error.localizedDescription)
                }
            }
        }
    }

    /// Resetea el estado de subida de documentos.
    func resetUploadState() {
        uploadState = .idle
    }

    // MARK: - Reservas

    /// Cancela una reserva y elimina la notificación programada.
    func cancelUserBooking(bookingId: Int32, completion: (() -> Void)? = nil) {
        userUseCase.cancelUserBooking(bookingId: bookingId) { _, error in
            let center = UNUserNotificationCenter.current()
            if error == nil {
                center.removePendingNotificationRequests(
                    withIdentifiers: ["booking_\(bookingId)"]
                )
            } else {
                print("Error al cancelar reserva: \(error!)")
            }
            DispatchQueue.main.async {
                completion?()
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

}
