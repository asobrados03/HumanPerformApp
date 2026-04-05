import Foundation
import shared

@MainActor
final class CalendarReservationFlowViewModel: ObservableObject {
    @Published var selectedProduct: Product?
    @Published var selectedHour: String?
    @Published var selectedCoach: DaySession?
    @Published var dialog: CalendarDialog = .hidden

    func selectDate() {
        selectedProduct = nil
        selectedHour = nil
        selectedCoach = nil
        dialog = .hidden
    }

    func selectProduct(_ product: Product?) {
        selectedProduct = product
        selectedHour = nil
        selectedCoach = nil
    }

    func selectHour(_ hour: String, coach: DaySession) {
        selectedHour = hour
        selectedCoach = coach
        dialog = .confirmBooking
    }

    func dismissDialog() {
        dialog = .hidden
    }
}

enum CalendarDialog {
    case hidden
    case confirmContinue
    case reservation
    case confirmBooking
    case changeExisting
}
