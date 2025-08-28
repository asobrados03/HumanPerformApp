import Foundation
import SwiftUI
import shared

/// ViewModel encargado de gestionar la lógica de la pantalla de favoritos en iOS.
/// Se apoya en el módulo KMM para obtener los entrenadores disponibles,
/// recuperar el entrenador favorito actual y marcar uno nuevo.
final class FavoritesViewModel: ObservableObject {
    /// Listado de profesionales disponibles.
    @Published var coaches: [Professional] = []
    /// Identificador del entrenador favorito del usuario.
    @Published var preferredCoachId: Int32? = nil
    /// Indicador de carga para la obtención de datos.
    @Published var isLoading: Bool = false
    /// Mensaje a mostrar al usuario tras marcar favorito o si ocurre un error.
    @Published var alertMessage: String? = nil

    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())

    /// Recupera la lista de entrenadores desde el backend.
    func loadCoaches() {
        isLoading = true
        userUseCase.getCoaches { [weak self] coaches, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                if let coaches = coaches {
                    self?.coaches = coaches
                } else if let error = error {
                    self?.alertMessage = error.localizedDescription
                }
            }
        }
    }

    /// Obtiene el entrenador favorito del usuario autenticado.
    func loadPreferredCoach(for userId: Int32) {
        userUseCase.getPreferredCoach(customerId: userId) { [weak self] response, error in
            DispatchQueue.main.async {
                if let response = response {
                    self?.preferredCoachId = response.coachId
                } else if let error = error {
                    self?.alertMessage = error.localizedDescription
                }
            }
        }
    }

    /// Marca como favorito al entrenador seleccionado y actualiza el favorito actual.
    func markFavorite(coach: Professional, userId: Int32?) {
        userUseCase.markFavorite(coachId: coach.id, serviceName: coach.service, userId: userId) { [weak self] message, error in
            DispatchQueue.main.async {
                if let message = message {
                    self?.alertMessage = message
                    if let uid = userId {
                        self?.loadPreferredCoach(for: uid)
                    }
                } else if let error = error {
                    self?.alertMessage = error.localizedDescription
                }
            }
        }
    }
}

