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
        userUseCase.getCoaches { [weak self] result, _ in
            DispatchQueue.main.async {
                self?.isLoading = false
                guard let result = result else { return }

                if let list = result.value as? [Professional] {
                    self?.coaches = list
                } else if let error = result.error {
                    self?.alertMessage = error.message
                }
            }
        }
    }

    /// Obtiene el entrenador favorito del usuario autenticado.
    func loadPreferredCoach(for userId: Int32) {
        userUseCase.getPreferredCoach(customerId: userId) { [weak self] result, _ in
            DispatchQueue.main.async {
                guard let result = result else { return }

                if let response = result.value as? GetPreferredCoachResponse {
                    self?.preferredCoachId = response.coachId
                } else if let error = result.error {
                    self?.alertMessage = error.message
                }
            }
        }
    }

    /// Marca como favorito al entrenador seleccionado y actualiza el favorito actual.
    func markFavorite(coach: Professional, userId: Int32?) {
        userUseCase.markFavorite(coachId: coach.id, serviceName: coach.service, userId: userId) { [weak self] result, _ in
            DispatchQueue.main.async {
                guard let result = result else { return }

                if let message = result.value as? String {
                    self?.alertMessage = message
                    if let uid = userId {
                        self?.loadPreferredCoach(for: uid)
                    }
                } else if let error = result.error {
                    self?.alertMessage = error.message
                }
            }
        }
    }
}

