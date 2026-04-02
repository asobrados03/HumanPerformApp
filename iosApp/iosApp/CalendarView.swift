import SwiftUI
import shared

struct CalendarView: View {
    @State private var daySessionViewModel = SharedDependencies.shared.makeDaySessionViewModel()
    @State private var sessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var bookingsViewModel = SharedDependencies.shared.makeUserBookingsViewModel()
    @State private var serviceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()

    @State private var currentMonth: Date = Date()
    @State private var selectedDate: Date = Date()

    @State private var selectedProduct: Product?
    @State private var selectedHour: String?
    @State private var selectedCoach: DaySession?
    @State private var dialog: CalendarDialog = .hidden
    @State private var bookingMenuTarget: UserBooking?
    @State private var bookingsFilter: String = "Todos"

    private let calendar = Calendar.current

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                if UITestConfig.isMockNetworkEnabled {
                    Text("Mock Calendar Loaded")
                        .accessibilityIdentifier("calendarLoadedMarker")
                }
                monthHeader
                weekDays
                calendarGrid

                if let state = bookingsViewModel.userBookings as? FetchUserBookingsStateLoading {
                    _ = state
                    ProgressView().padding(.top, 12)
                } else if let error = bookingsViewModel.userBookings as? FetchUserBookingsStateError {
                    Text("Error al cargar: \(error.message)")
                        .foregroundColor(.red)
                } else if let success = bookingsViewModel.userBookings as? FetchUserBookingsStateSuccess {
                    userBookingsSection(bookings: success.bookings)
                }
            }
            .padding(.horizontal)
            .padding(.bottom, 24)
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear {
            if UITestConfig.isMockNetworkEnabled { return }
            bootstrap()
        }
        .onChange(of: selectedDate) { _ in
            selectedProduct = nil
            daySessionViewModel.clearSessions()
        }
        .sheet(isPresented: Binding(
            get: { dialog == .reservation },
            set: { if !$0 { dismissDialog() } }
        )) {
            reservationSheet
                .presentationDetents([.medium, .large])
        }
        .alert("Aviso", isPresented: Binding(
            get: { dialog == .confirmContinue },
            set: { if !$0 { dismissDialog() } }
        )) {
            Button("No", role: .cancel) { dismissDialog() }
            Button("Sí") { dialog = .reservation }
        } message: {
            Text("Ya tienes una reserva hoy. ¿Continuar?")
        }
        .alert("Confirmar reserva", isPresented: Binding(
            get: { dialog == .confirmBooking },
            set: { if !$0 { dismissDialog() } }
        )) {
            Button("Cancelar", role: .cancel) { dismissDialog() }
            Button("Reservar") { submitBooking() }
            Button("Cambiar") { dialog = .changeExisting }
        } message: {
            Text("¿Deseas confirmar tu sesión con \(selectedCoach?.coachName ?? "-") a las \(selectedHour?.prefix(5) ?? "")?")
        }
        .accessibilityIdentifier("calendarView")
        .confirmationDialog(
            "Gestión de reserva",
            isPresented: Binding(get: { dialog == .changeExisting }, set: { if !$0 { dismissDialog() } }),
            titleVisibility: .visible
        ) {
            if let success = bookingsViewModel.userBookings as? FetchUserBookingsStateSuccess {
                ForEach(success.bookings, id: \.id) { booking in
                    Button("Cambiar #\(booking.id) (\(booking.date.prefix(10)) \(booking.hour.prefix(5)))") {
                        submitBookingChange(booking: booking)
                    }
                }
            }
            Button("Cancelar", role: .cancel) { dismissDialog() }
        }
        .confirmationDialog(
            "Reserva",
            isPresented: Binding(get: { bookingMenuTarget != nil }, set: { if !$0 { bookingMenuTarget = nil } }),
            titleVisibility: .visible
        ) {
            if let booking = bookingMenuTarget {
                Button("Descargar evento") { downloadICS(for: booking) }
                Button("Cancelar reserva", role: .destructive) {
                    bookingsViewModel.cancelUserBooking(bookingId: booking.id, currentUser: sessionViewModel.userData)
                    refreshBookings()
                }
            }
            Button("Cerrar", role: .cancel) { bookingMenuTarget = nil }
        }
    }

    private func bootstrap() {
        daySessionViewModel.fetchHolidays()
        if let userId = sessionViewModel.userData?.id {
            bookingsViewModel.fetchUserBookings(userId: userId)
            serviceProductViewModel.loadUserProducts(userId: userId)
        }
    }

    private var monthHeader: some View {
        HStack {
            Button(action: previousMonth) { Image(systemName: "chevron.left") }
            Spacer()
            Text(monthYearString(currentMonth)).font(.title3).bold()
            Spacer()
            Button(action: nextMonth) { Image(systemName: "chevron.right") }
        }
        .padding(.top, 8)
    }

    private var weekDays: some View {
        HStack {
            ForEach(["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"], id: \.self) { day in
                Text(day).font(.caption).bold().frame(maxWidth: .infinity)
            }
        }
    }

    private var calendarGrid: some View {
        let days = generateDays(for: currentMonth)
        let columns = Array(repeating: GridItem(.flexible()), count: 7)
        let today = Date()

        return LazyVGrid(columns: columns, spacing: 8) {
            ForEach(Array(days.enumerated()), id: \.offset) { _, date in
                if let date {
                    let isHoliday = isHolidayDate(date)
                    let isReserved = isReservedDate(date)
                    let selectable = isSelectable(date: date, today: today, isHoliday: isHoliday)

                    Text("\(calendar.component(.day, from: date))")
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity, minHeight: 34)
                        .padding(.vertical, 3)
                        .background(backgroundColor(date: date, isReserved: isReserved, isHoliday: isHoliday, selectable: selectable))
                        .clipShape(Circle())
                        .overlay(
                            Circle().stroke(calendar.isDateInToday(date) ? Color.secondary : Color.clear, lineWidth: 2)
                        )
                        .opacity(selectable ? 1 : 0.45)
                        .onTapGesture {
                            guard selectable else { return }
                            selectedDate = date
                            onDayClicked(date)
                        }
                } else {
                    Color.clear.frame(height: 36)
                }
            }
        }
    }

    @ViewBuilder
    private func userBookingsSection(bookings: [UserBooking]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Mis sesiones reservadas")
                .font(.headline)

            let filterValues = ["Todos"] + Array(Set(bookings.map { $0.service })).sorted()
            Picker("Servicio", selection: $bookingsFilter) {
                ForEach(filterValues, id: \.self) { Text($0).tag($0) }
            }
            .pickerStyle(.menu)

            let filtered = bookings.filter { bookingsFilter == "Todos" || $0.service == bookingsFilter }
            if filtered.isEmpty {
                Text("No tienes sesiones reservadas.")
                    .foregroundColor(.secondary)
            } else {
                ForEach(filtered, id: \.id) { booking in
                    VStack(alignment: .leading, spacing: 4) {
                        Text("📅 \(booking.date.prefix(10)) - 🕒 \(booking.hour.prefix(5))")
                        Text("🏢 Servicio: \(booking.service)")
                        Text("✨ Producto: \(booking.product)")
                        Text("👟 Profesional: \(booking.coachName)")
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(10)
                    .onTapGesture { bookingMenuTarget = booking }
                }
            }
        }
    }

    private var reservationSheet: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Seleccionar servicio").font(.headline)

            if let productsState = serviceProductViewModel.userProductsState as? UserProductsUiStateSuccess {
                Picker("Servicio", selection: Binding(
                    get: { selectedProduct?.id ?? -1 },
                    set: { newId in
                        selectedProduct = productsState.products.first(where: { $0.id == newId })
                        if let product = selectedProduct {
                            daySessionViewModel.fetchAvailableSessions(productId: product.id, date: kmmDate(from: selectedDate))
                        }
                    }
                )) {
                    Text("Seleccionar").tag(-1)
                    ForEach(productsState.products, id: \.id) { p in Text(p.name).tag(p.id) }
                }
                .pickerStyle(.menu)
            }

            sessionsSelector
            Spacer()
        }
        .padding()
    }

    @ViewBuilder
    private var sessionsSelector: some View {
        if selectedProduct == nil {
            Text("Selecciona un servicio para ver horarios.")
                .foregroundColor(.secondary)
        } else if daySessionViewModel.sessions is DailySessionsUiStateLoading {
            ProgressView().frame(maxWidth: .infinity)
        } else if let success = daySessionViewModel.sessions as? DailySessionsUiStateSuccess {
            let hours = Array(Set(success.sessions.map { $0.hour })).sorted()
            ForEach(hours, id: \.self) { hour in
                Button(hour.prefix(5)) {
                    let coaches = daySessionViewModel.getAvailableCoachesForHour(hour: hour)
                    if let coach = coaches.first {
                        selectedHour = hour
                        selectedCoach = coach
                        dialog = .confirmBooking
                    }
                }
                .buttonStyle(.borderedProminent)
            }
        } else if daySessionViewModel.sessions is DailySessionsUiStateEmpty {
            Text("No hay horarios disponibles.")
                .foregroundColor(.secondary)
        } else if let error = daySessionViewModel.sessions as? DailySessionsUiStateError {
            Text(error.message).foregroundColor(.red)
        }
    }

    private func onDayClicked(_ date: Date) {
        if isReservedDate(date) {
            dialog = .confirmContinue
        } else {
            dialog = .reservation
        }
    }

    private func submitBooking() {
        guard
            let userId = sessionViewModel.userData?.id,
            let product = selectedProduct,
            let hour = selectedHour,
            let coach = selectedCoach
        else { return }

        daySessionViewModel.fetchServiceIdForProductAsync(productId: product.id) { serviceId in
            guard let serviceId, serviceId > 0 else { return }
            daySessionViewModel.makeBookingAsync(
                customerId: userId,
                coachId: coach.coachId,
                serviceId: serviceId,
                productId: product.id,
                dayOfWeek: englishDay(from: selectedDate),
                centerId: 1,
                selectedDate: ymd(selectedDate),
                hour: hour
            ) { _ in
                refreshBookings()
                dismissDialog()
            }
        }
    }

    private func submitBookingChange(booking: UserBooking) {
        guard let product = selectedProduct, let hour = selectedHour, let coach = selectedCoach else { return }

        daySessionViewModel.fetchServiceIdForProductAsync(productId: product.id) { serviceId in
            guard let serviceId, serviceId > 0 else { return }
            daySessionViewModel.modifyBookingSessionAsync(
                bookingId: booking.id,
                newCoachId: coach.coachId,
                newServiceId: serviceId,
                newProductId: product.id,
                newDayOfWeek: englishDay(from: selectedDate),
                newStartDate: ymd(selectedDate),
                hour: hour
            ) { _ in
                refreshBookings()
                dismissDialog()
            }
        }
    }

    private func refreshBookings() {
        if let userId = sessionViewModel.userData?.id {
            bookingsViewModel.fetchUserBookings(userId: userId)
        }
    }

    private func dismissDialog() {
        dialog = .hidden
    }

    private func isHolidayDate(_ date: Date) -> Bool {
        daySessionViewModel.holidays.contains {
            $0.year == Int32(calendar.component(.year, from: date)) &&
            $0.monthNumber == Int32(calendar.component(.month, from: date)) &&
            $0.dayOfMonth == Int32(calendar.component(.day, from: date))
        }
    }

    private func isReservedDate(_ date: Date) -> Bool {
        guard let success = bookingsViewModel.userBookings as? FetchUserBookingsStateSuccess else { return false }
        return success.bookings.contains { booking in
            String(booking.date.prefix(10)) == ymd(date)
        }
    }

    private func backgroundColor(date: Date, isReserved: Bool, isHoliday: Bool, selectable: Bool) -> Color {
        if isReserved { return Color.green.opacity(0.75) }
        if isHoliday || calendar.isDateInWeekend(date) { return Color.clear }
        return selectable ? Color(.systemBackground) : Color(.systemGray5)
    }

    private func isSelectable(date: Date, today: Date, isHoliday: Bool) -> Bool {
        if isHoliday || calendar.isDateInWeekend(date) || date < calendar.startOfDay(for: today) { return false }

        let day = calendar.component(.day, from: today)
        let hour = calendar.component(.hour, from: Date())
        let isAfter15 = day > 15 || (day == 15 && hour >= 12)

        let monthOfDate = calendar.component(.month, from: date)
        let yearOfDate = calendar.component(.year, from: date)
        let currentMonth = calendar.component(.month, from: today)
        let currentYear = calendar.component(.year, from: today)

        let nextMonthDate = calendar.date(byAdding: .month, value: 1, to: today) ?? today
        let nextMonth = calendar.component(.month, from: nextMonthDate)
        let nextYear = calendar.component(.year, from: nextMonthDate)

        if day < 15 {
            return monthOfDate == currentMonth && yearOfDate == currentYear
        }

        return (monthOfDate == currentMonth && yearOfDate == currentYear) ||
            (isAfter15 && monthOfDate == nextMonth && yearOfDate == nextYear)
    }

    private func downloadICS(for booking: UserBooking) {
        let hour = booking.hour.count == 5 ? booking.hour + ":00" : booking.hour
        let iso = String(booking.date.prefix(10)) + "T" + hour
        if let date = ISO8601DateFormatter().date(from: iso) {
            shareICS(content: createICSFile(eventTitle: booking.service, startDate: date))
        }
    }

    private func kmmDate(from date: Date) -> Kotlinx_datetimeLocalDate {
        Kotlinx_datetimeLocalDate(
            year: Int32(calendar.component(.year, from: date)),
            monthNumber: Int32(calendar.component(.month, from: date)),
            dayOfMonth: Int32(calendar.component(.day, from: date))
        )
    }

    private func englishDay(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US")
        formatter.dateFormat = "EEEE"
        return formatter.string(from: date)
    }

    private func ymd(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }

    private func generateDays(for date: Date) -> [Date?] {
        var days: [Date?] = []
        let startOfMonth = calendar.date(from: calendar.dateComponents([.year, .month], from: date))!

        var firstWeekday = calendar.component(.weekday, from: startOfMonth)
        firstWeekday = firstWeekday == 1 ? 7 : firstWeekday - 1

        for _ in 1..<firstWeekday { days.append(nil) }

        let range = calendar.range(of: .day, in: .month, for: startOfMonth)!
        for day in range {
            days.append(calendar.date(byAdding: .day, value: day - 1, to: startOfMonth))
        }
        return days
    }

    private func monthYearString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_ES")
        formatter.dateFormat = "LLLL yyyy"
        return formatter.string(from: date).capitalized
    }

    private func previousMonth() {
        currentMonth = calendar.date(byAdding: .month, value: -1, to: currentMonth) ?? currentMonth
    }

    private func nextMonth() {
        currentMonth = calendar.date(byAdding: .month, value: 1, to: currentMonth) ?? currentMonth
    }
}

private enum CalendarDialog {
    case hidden
    case confirmContinue
    case reservation
    case confirmBooking
    case changeExisting
}
