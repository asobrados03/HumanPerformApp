import Foundation
import shared

@MainActor
final class CalendarReservationFlowViewModel: ObservableObject {
    @Published var selectedProduct: Product?
    @Published var selectedHour: String?
    @Published var selectedCoach: DaySession?
    @Published var availableCoaches: [DaySession] = []
    @Published var dialog: CalendarDialog = .hidden

    func selectDate() {
        selectedProduct = nil
        selectedHour = nil
        selectedCoach = nil
        availableCoaches = []
        dialog = .hidden
    }

    func selectProduct(_ product: Product?) {
        selectedProduct = product
        selectedHour = nil
        selectedCoach = nil
        availableCoaches = []
    }

    func selectHour(_ hour: String, coaches: [DaySession]) {
        selectedHour = hour
        selectedCoach = nil
        availableCoaches = coaches
        dialog = coaches.isEmpty ? .hidden : .selectCoach
    }

    func selectCoach(_ coach: DaySession) {
        selectedCoach = coach
        dialog = .confirmBooking
    }

    func dismissDialog() {
        if dialog == .selectCoach {
            availableCoaches = []
        }
        dialog = .hidden
    }
}

enum CalendarDialog {
    case hidden
    case confirmContinue
    case reservation
    case selectCoach
    case confirmBooking
    case changeExisting
}
