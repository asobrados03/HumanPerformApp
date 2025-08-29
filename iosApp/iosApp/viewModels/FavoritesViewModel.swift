import Foundation
import SwiftUI
import shared

final class FavoritesViewModel: ObservableObject {
    @Published var coaches: [Professional] = []
    @Published var preferredCoachId: Int32? = nil
    @Published var isLoading: Bool = false
    @Published var alertMessage: String? = nil

    // Usa tu UserUseCase habitual; ahora con las funciones *Raw* sin wrapper Result<T>.
    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())

    // MARK: - Carga de datos

    func loadCoaches() {
        isLoading = true
        userUseCase.getCoachesRaw { [weak self] value, error in
            DispatchQueue.main.async {
                guard let self else { return }
                self.isLoading = false

                if let err = error as? KotlinThrowable {
                    self.alertMessage = err.message ?? "Unknown error"
                    return
                }

                if let list = value {
                    self.coaches = list
                } else if let nsErr = error {
                    self.alertMessage = nsErr.localizedDescription
                } else {
                    self.alertMessage = "Respuesta inválida."
                }
            }
        }
    }

    func loadPreferredCoach(for userId: Int32) {
        userUseCase.getPreferredCoachRaw(customerId: Int32(Int(truncatingIfNeeded: userId))) { [weak self] value, error in
            DispatchQueue.main.async {
                guard let self else { return }

                if let err = error as? KotlinThrowable {
                    self.alertMessage = err.message ?? "Unknown error"
                    return
                }

                if let resp = value {
                    self.preferredCoachId = resp.coachId
                } else if let nsErr = error {
                    self.alertMessage = nsErr.localizedDescription
                }
            }
        }
    }

    func markFavorite(coach: Professional, userId: Int32?) {
        // Tipos explícitos para evitar ambigüedad con el puente KMM
        let coachId32: Int32 = coach.id
        let kUserId: KotlinInt? = userId.map { KotlinInt(int: $0) }

        userUseCase.markFavoriteRaw(
            coachId: coachId32,
            serviceName: coach.service,
            userId: kUserId
        ) { [weak self] (value: Any?, error: Error?) in
            DispatchQueue.main.async { [weak self] in
                // ✅ Sin guard. Solo optional chaining.
                if let err = error as? KotlinThrowable {
                    self?.alertMessage = err.message ?? "Unknown error"
                    return
                }

                if let msg = value as? String {
                    self?.alertMessage = msg
                    if let uid = userId {
                        self?.loadPreferredCoach(for: uid)
                    }
                } else if let nsErr = error {
                    self?.alertMessage = nsErr.localizedDescription
                } else {
                    self?.alertMessage = "Respuesta inválida."
                }
            }
        }
    }
}
