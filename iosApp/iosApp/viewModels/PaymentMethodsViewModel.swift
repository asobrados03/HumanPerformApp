import Foundation
import SwiftUI
import shared

enum PaymentMethodsUiState {
    case idle
    case loading
    case success([PaymentMethod])
    case empty
    case error(String)
}

/// ViewModel nativo para gestionar la lógica de la pantalla de métodos de pago.
/// No expone `Result` de Kotlin; usa directamente el tipo devuelto o un `Error`.
final class PaymentMethodsViewModel: ObservableObject {
    @Published var uiState: PaymentMethodsUiState = .idle

    private let paymentUseCase = PaymentUseCase(paymentRepository: PaymentRepositoryImpl())

    /// Obtiene los métodos de pago del usuario.
    func loadMethods(for userId: Int32) {
        uiState = .loading
        paymentUseCase.getPaymentMethods(userId: userId) { [weak self] methods, error in
            DispatchQueue.main.async {
                guard let self else { return }

                if let error = error {
                    self.uiState = .error(error.localizedDescription)
                } else if let methods = methods, !methods.isEmpty {
                    self.uiState = .success(methods)
                } else {
                    self.uiState = .empty
                }
            }
        }
    }
}

