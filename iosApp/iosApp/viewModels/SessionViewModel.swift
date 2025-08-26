//
//  SessionViewModel.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import shared
import KMPNativeCoroutinesAsync

class SessionViewModel: ObservableObject {
    // Datos básicos de sesión del usuario
    @Published var accessToken: String? = nil
    @Published var userId: Int? = nil
    @Published var userEmail: String? = nil
    @Published var userName: String? = nil
    @Published var userStreet: String? = nil
    @Published var userPostalCode: Int? = nil

    // Repositorio o use case relacionado a sesión, si existe en KMM (por ejemplo, SessionRepository)
    //private let sessionRepository = SessionRepository()  // (Si hay implementación compartida)
    // O podríamos usar UserUseCase para ciertas operaciones de sesión (como allowed services):
    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())
    
    @Published var isLoggedIn: Bool? = nil

    private var observeTask: Task<Void, Never>?

    /// Actualiza las credenciales del usuario en la sesión (llamar tras login)
    func setUserCredentials(token: String, id: Int, email: String, name: String, street: String, postalCode: Int?) {
        DispatchQueue.main.async {
            self.accessToken = token
            self.userId = id
            self.userEmail = email
            self.userName = name
            self.userStreet = street
            self.userPostalCode = postalCode
        }
        // Podemos almacenar el token en el SecureStorage compartido, si corresponde:
        
    }

    func startObserving() {
            observeTask?.cancel()
            observeTask = Task {
                do {
                    // Si tu SecureStorage se expone como .shared, usa la línea de abajo:
                    for try await token in asyncSequence(for: SecureStorage.shared.accessTokenFlow()) {
                        await MainActor.run {
                            print("token", token)
                            self.isLoggedIn = !token.isEmpty
                        }
                    }
                } catch is CancellationError {
                    // ignorar cancelaciones
                } catch {
                    // ante error, considera tratar como "no logueado"
                    await MainActor.run { self.isLoggedIn = false }
                }
            }
        }

        deinit { observeTask?.cancel() }

    /// (Opcional) Carga servicios permitidos para el usuario (similar a Android)
    func loadAllowedServicesForUser() {
        guard let id = userId else { return }
        userUseCase.getUserAllowedServices(customerId: Int32(id)) { services, error in
            if let svcList = services {
                print("Servicios permitidos recibidos: \(svcList.count) servicios")
                // Aquí podríamos publicarlos vía @Published si quisiéramos exponerlos en la UI
                // Por ejemplo: @Published var allowedServices: [ServiceAvailable] = []
                // DispatchQueue.main.async { self.allowedServices = svcList }
            } else if let error = error {
                print("❌ Error al cargar servicios permitidos: \(error.localizedDescription)")
            }
        }
    }

    /// Logout del usuario: limpia datos de sesión y token.
    func logout() {
        // Llamar al repositorio compartido si hay que hacer logout en backend o limpiar algo en KMM
        //sessionRepository.logout()  // P. ej., borrar sesiones almacenadas localmente en la base de datos, etc.
        // Limpiar datos locales
        DispatchQueue.main.async {
            self.accessToken = nil
            self.userId = nil
            self.userEmail = nil
            self.userName = nil
            self.userStreet = nil
            self.userPostalCode = nil
        }
    }
}
