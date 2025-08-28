import Foundation
import UserNotifications
import shared

/// ViewModel de iOS para manejar las sesiones disponibles en un día.
/// Replica la lógica avanzada existente en Android, incluyendo gestión de
/// límites semanales, cuestionarios de bienestar, notificaciones y cambios de
/// reserva.
class DaySessionViewModel: ObservableObject {
    private let useCase = DaySessionUseCase(repository: DaySessionRepositoryImpl())
    private let userUseCase = UserUseCase(userRepository: UserRepositoryImpl())

    /// Sesiones disponibles para la fecha seleccionada.
    @Published var sessions: [DaySession] = []

    /// Resultado de la última operación: "success", "updated", "limit" o "error".
    @Published var reservationResult: String? = nil

    // MARK: - Gestión de límites semanales
    @Published var weeklyLimits: [Int32: Int32] = [:]
    @Published var unlimitedSessions: [Int32: Int32] = [:]
    @Published var sharedSessions: [SharedPool] = []
    @Published var serviceToPrimary: [Int32: Int32] = [:]
    @Published var validFromByPrimary: [Int32: String] = [:]
    @Published var userBookings: [UserBooking] = []

    // MARK: - Cuestionario
    @Published var questionnaireActive = false
    @Published var currentQuestion = 0
    var responses: [String?] = Array(repeating: nil, count: 5)
    private var pendingBookingId: Int32? = nil
    var skippedSessions: [Int32] = []

    // MARK: - Festivos
    @Published var holidays: [Date] = []

    // MARK: - Wrappers KMM -> async/await

    private func getSessionsByDayAsync(serviceId: Int32, date: Kotlinx_datetimeLocalDate) async throws -> [DaySession] {
        try await withCheckedThrowingContinuation { cont in
            useCase.getSessionsByDay(serviceId: serviceId, date: date) { data, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: data ?? []) }
            }
        }
    }

    private func getUserBookingsAsync(customerId: Int32) async throws -> [UserBooking] {
        try await withCheckedThrowingContinuation { cont in
            userUseCase.getUserBookings(customerId: customerId) { data, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: data ?? []) }
            }
        }
    }

    private func getUserProductIdAsync(customerId: Int32) async throws -> Int32 {
        try await withCheckedThrowingContinuation { cont in
            useCase.getUserProductId(customerId: customerId) { value, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: value?.int32Value ?? 0) } // 👈 Int32
            }
        }
    }

    private func getTimeslotIdAsync(hour: String) async throws -> Int32 {
        try await withCheckedThrowingContinuation { cont in
            useCase.getTimeslotId(hora: hour) { value, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: value?.int32Value ?? 0) } // 👈 Int32
            }
        }
    }

    private func reservarSesionAsync(request: ReserveRequest) async throws {
        try await withCheckedThrowingContinuation { (cont: CheckedContinuation<Void, Error>) in
            useCase.reservarSesion(request: request) { _, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: ()) }
            }
        }
    }

    private func cambiarReservaSesionAsync(request: ReserveUpdateRequest) async throws {
        try await withCheckedThrowingContinuation { (cont: CheckedContinuation<Void, Error>) in
            useCase.cambiarReservaSesion(request: request) { _, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: ()) }
            }
        }
    }

    private func getUserWeeklyLimitAsync(userId: Int32) async throws -> UserWeeklyLimitResponse {
        try await withCheckedThrowingContinuation { cont in
            useCase.getUserWeeklyLimit(customerId: userId) { data, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: data!) } // La API debería devolver siempre algo
            }
        }
    }

    private func getHolidaysAsync() async throws -> [String] {
        try await withCheckedThrowingContinuation { cont in
            useCase.getHolidays { data, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: data ?? []) }
            }
        }
    }

    private func cuestionarioEnviadoAsync(bookingId: Int32) async throws -> Bool {
        try await withCheckedThrowingContinuation { cont in
            useCase.cuestionarioEnviado(bookingId: bookingId) { value, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: value?.boolValue ?? false) }
            }
        }
    }

    private func enviarCuestionarioReservaAsync(form: BookingQuestionnaireRequest) async throws {
        try await withCheckedThrowingContinuation { (cont: CheckedContinuation<Void, Error>) in
            useCase.enviarCuestionarioReserva(bookingForm: form) { _, error in
                if let error = error { cont.resume(throwing: error) }
                else { cont.resume(returning: ()) }
            }
        }
    }

    // MARK: - Sesiones
    func fetchSessions(serviceId: Int32, date: Date) {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: date)
        guard let year = comps.year, let month = comps.month, let day = comps.day else { return }
        let kotlinDate = Kotlinx_datetimeLocalDate(year: Int32(year), month: Int32(month), day: Int32(day)) // ✅ day:

        Task {
            do {
                let result = try await getSessionsByDayAsync(serviceId: serviceId, date: kotlinDate)
                await MainActor.run { self.sessions = result }
            } catch {
                print("Error al obtener sesiones: \(error)")
                await MainActor.run { self.sessions = [] }
            }
        }
    }

    // MARK: - Reservas
    func reserveSession(
        customerId: Int32,
        coachId: Int32,
        serviceId: Int32,
        centerId: Int32,
        date: Date,
        hour: String
    ) {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: date)
        guard let year = comps.year, let month = comps.month, let day = comps.day else { return }
        let isoDate = String(format: "%04d-%02d-%02dT%@", year, month, day, hour)

        Task {
            do {
                let bookings = try await getUserBookingsAsync(customerId: customerId)
                if exceedsWeeklyLimit(serviceId: serviceId, selectedDate: date, bookings: bookings) {
                    await MainActor.run { self.reservationResult = "limit" }
                    return
                }

                let productId = try await getUserProductIdAsync(customerId: customerId)
                let timeslotId = try await getTimeslotIdAsync(hour: hour)

                let request = ReserveRequest(
                    customer_id: Int32(customerId),
                    coach_id: Int32(coachId),
                    session_timeslot_id: Int32(timeslotId),
                    service_id: Int32(serviceId),
                    product_id: Int32(productId),
                    center_id: Int32(centerId),
                    start_date: isoDate,
                    status: "active",
                    payment_status: "pending",
                    payment_method: "card"
                )

                try await reservarSesionAsync(request: request)

                await MainActor.run {
                    self.reservationResult = "success"
                    self.refreshUserBookings(userId: customerId)
                }
            } catch {
                print("Error al reservar: \(error)")
                await MainActor.run { self.reservationResult = "error" }
            }
        }
    }

    func updateReservation(
        customerId: Int32,
        bookingId: Int32,
        newCoachId: Int32,
        newServiceId: Int32,
        date: Date,
        hour: String
    ) {
        let comps = Calendar.current.dateComponents([.year, .month, .day], from: date)
        guard let year = comps.year, let month = comps.month, let day = comps.day else { return }
        let isoDate = String(format: "%04d-%02d-%02dT%@", year, month, day, hour)

        Task {
            do {
                let productId = try await getUserProductIdAsync(customerId: customerId)
                let timeslotId = try await getTimeslotIdAsync(hour: hour)

                let request = ReserveUpdateRequest(
                    booking_id: Int32(bookingId),
                    new_coach_id: Int32(newCoachId),
                    new_service_id: Int32(newServiceId),
                    new_product_id: Int32(productId),
                    new_session_timeslot_id: Int32(timeslotId),
                    new_start_date: isoDate
                )

                try await cambiarReservaSesionAsync(request: request)

                await MainActor.run {
                    self.reservationResult = "updated"
                    self.refreshUserBookings(userId: customerId)
                }
            } catch {
                print("Error al actualizar reserva: \(error)")
                await MainActor.run { self.reservationResult = "error" }
            }
        }
    }

    // MARK: - Límite semanal
    func fetchUserWeeklyLimit(userId: Int32) {
        Task {
            do {
                let response = try await getUserWeeklyLimitAsync(userId: userId)
                await MainActor.run {
                    self.weeklyLimits = response.weekly_limit as! [Int32: Int32]
                    self.unlimitedSessions = response.unlimited_sessions as! [Int32: Int32]
                    self.sharedSessions = response.unlimited_shared
                    self.serviceToPrimary = response.service_to_primary as! [Int32: Int32]
                    self.validFromByPrimary = response.valid_from_by_primary as! [Int32: String]
                }
            } catch {
                print("Error al cargar límites semanales: \(error)")
            }
        }
    }

    func refreshUserBookings(userId: Int32, completion: (() -> Void)? = nil) {
        Task {
            do {
                let bookings = try await getUserBookingsAsync(customerId: userId)
                await MainActor.run {
                    self.userBookings = bookings
                    self.loadQuestionnaireIfNeeded()
                    completion?()
                }
            } catch {
                print("Error al obtener reservas: \(error)")
            }
        }
    }

    private func exceedsWeeklyLimit(serviceId: Int32, selectedDate: Date, bookings: [UserBooking]) -> Bool {
        let primaryTarget = serviceToPrimary[serviceId] ?? serviceId
        let cal = Calendar.current
        let weekday = cal.component(.weekday, from: selectedDate)
        let diff = (weekday + 5) % 7
        guard let weekStart = cal.date(byAdding: .day, value: -diff, to: selectedDate),
              let weekEnd = cal.date(byAdding: .day, value: 6, to: weekStart) else { return false }

        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"

        if let limit = weeklyLimits[primaryTarget] {
            let count = bookings.filter { booking in
                let primary = serviceToPrimary[Int32(truncating: booking.service_id ?? 0)] ?? Int32(truncating: booking.service_id ?? 0)
                guard primary == primaryTarget,
                      let date = formatter.date(from: String(booking.date.prefix(10))) else { return false }
                return date >= weekStart && date <= weekEnd
            }.count
            return count >= limit
        }

        let pools = sharedSessions.filter { pool in
            pool.services.contains(KotlinInt(value: Int32(primaryTarget)))   // 👈 asegurar Int32
        }
        let validFromPrimaryDate = validFromByPrimary[primaryTarget].flatMap {
            formatter.date(from: String($0.prefix(10)))
        }

        let usedTotal = bookings.filter { b in
            let primary = serviceToPrimary[Int32(truncating: b.service_id ?? 0)] ?? Int32(truncating: b.service_id ?? 0)
            guard primary == primaryTarget,
                  let f = formatter.date(from: String(b.date.prefix(10))) else { return false }
            if let vf = validFromPrimaryDate { return f >= vf } else { return true }
        }.count

        let dedicatedAllowed = unlimitedSessions[primaryTarget]
        let dedicatedRemaining = dedicatedAllowed != nil ? max((dedicatedAllowed! - Int32(usedTotal)), 0) : 0

        var sharedRemaining: Int32 = 0
        if !pools.isEmpty {
            for pool in pools {
                let vf = pool.valid_from.flatMap { formatter.date(from: String($0.prefix(10))) }
                let vt = pool.valid_to.flatMap { formatter.date(from: String($0.prefix(10))) }
                let usedInPool = bookings.filter { b in
                    let primary = serviceToPrimary[Int32(truncating: b.service_id ?? 0)] ?? Int32(truncating: b.service_id ?? 0)
                    guard pool.services.contains(KotlinInt(value: Int32(primary))),      // 👈 asegurar Int32
                          let f = formatter.date(from: String(b.date.prefix(10))) else { return false }
                    if let vf = vf, f < vf { return false }
                    if let vt = vt, f > vt { return false }
                    return true
                }.count
                sharedRemaining += max(pool.sessions - Int32(usedInPool), Int32(0))
            }
        }

        if dedicatedAllowed == nil && pools.isEmpty { return false }

        let totalAvailable = max(dedicatedRemaining + sharedRemaining, 0)
        return totalAvailable <= 0
    }

    // MARK: - Festivos
    func fetchHolidays() {
        Task {
            do {
                let result = try await getHolidaysAsync()
                let formatter = ISO8601DateFormatter()
                let dates = result.compactMap { formatter.date(from: $0) }
                await MainActor.run { self.holidays = dates }
            } catch {
                print("Error al obtener festivos: \(error)")
            }
        }
    }

    // MARK: - Cuestionario
    private func loadQuestionnaireIfNeeded() {
        let now = Date()
        let formatter = ISO8601DateFormatter()
        guard let session = userBookings.first(where: { booking in
            if skippedSessions.contains(Int32(booking.id)) { return false }
            let hour = booking.hour.count == 5 ? booking.hour + ":00" : booking.hour
            let iso = String(booking.date.prefix(10)) + "T" + hour
            guard let date = formatter.date(from: iso) else { return false }
            let diff = date.timeIntervalSince(now)
            return diff > 0 && diff <= 3600
        }) else { return }

        Task {
            do {
                let sent = try await cuestionarioEnviadoAsync(bookingId: Int32(session.id))
                if !sent {
                    await MainActor.run {
                        self.pendingBookingId = Int32(session.id)
                        self.questionnaireActive = true
                        self.currentQuestion = 0
                        self.responses = Array(repeating: nil, count: 5)
                    }
                }
            } catch {
                print("Error al verificar cuestionario: \(error)")
            }
        }
    }

    func answerQuestion(_ response: String) {
        responses[currentQuestion] = response
        if currentQuestion < 4 {
            currentQuestion += 1
        } else {
            sendQuestionnaire()
        }
    }

    func skipQuestionnaire() {
        questionnaireActive = false
        if let id = pendingBookingId { skippedSessions.append(id) }
    }

    private func sendQuestionnaire() {
        guard let bookingId = pendingBookingId else { return }
        let form = BookingQuestionnaireRequest(
            booking_id: Int32(bookingId),
            sleep_quality: responses[0] ?? "",
            energy_level: responses[1] ?? "",
            muscle_pain: responses[2] ?? "",
            stress_level: responses[3] ?? "",
            mood: responses[4] ?? ""
        )

        Task {
            do {
                try await enviarCuestionarioReservaAsync(form: form)
                await MainActor.run { self.questionnaireActive = false }
            } catch {
                print("Error al enviar cuestionario: \(error)")
            }
        }
    }

    // MARK: - Notificaciones
    func scheduleNotification(bookingId: Int32, hour: String, date: Date) {
        let center = UNUserNotificationCenter.current()
        center.getPendingNotificationRequests { requests in
            let identifier = "booking_\(bookingId)"
            guard !requests.contains(where: { $0.identifier == identifier }) else { return }

            center.requestAuthorization(options: [.alert, .sound]) { _, _ in }

            var comps = Calendar.current.dateComponents([.year, .month, .day], from: date)
            let parts = hour.split(separator: ":").map { Int($0) ?? 0 }
            comps.hour = parts.first
            comps.minute = parts.count > 1 ? parts[1] : 0

            let content = UNMutableNotificationContent()
            content.title = "Reserva confirmada"
            content.body = "Tu sesión comienza a las \(hour)"

            let trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)
            let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
            center.add(request, withCompletionHandler: nil)
        }
    }
}
