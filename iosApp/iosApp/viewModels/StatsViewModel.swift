import Foundation
import SwiftUI
import shared

/// ViewModel que obtiene las estadísticas del usuario desde el módulo compartido.
final class StatsViewModel: ObservableObject {
    @Published var entrenamientosMesPasado: Int = 0
    @Published var entrenadorMasUsado: String? = nil
    @Published var reservasPendientes: Int = 0
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())

    /// Carga las estadísticas del usuario.
    func loadStats(userId: Int32) {
        isLoading = true
        error = nil
        userUseCase.getUserStats(customerId: Int32(userId)) { [weak self] stats, err in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.isLoading = false
                if let stats = stats {
                    self.entrenamientosMesPasado = Int(truncatingIfNeeded: stats.entrenamientosMesPasado)
                    self.entrenadorMasUsado = stats.entrenadorMasUsado
                    self.reservasPendientes = Int(truncatingIfNeeded: stats.reservasPendientes)
                } else if let err = err {
                    self.error = err.localizedDescription
                } else {
                    self.error = "Error desconocido"
                }
            }
        }
    }
}
