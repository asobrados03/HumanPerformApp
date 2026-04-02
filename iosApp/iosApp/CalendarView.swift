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

    private var userProducts: [Product] {
        serviceProductViewModel.userProductsStateProducts()
    }

    private var sessionsStateName: String {
        String(describing: type(of: daySessionViewModel.sessions))
    }

    private var isReservationPresented: Binding<Bool> {
        Binding<Bool>(
            get: { dialog == .reservation },
            set: { if !$0 { dismissDialog() } }
        )
    }

    private var isConfirmContinuePresented: Binding<Bool> {
        Binding<Bool>(
            get: { dialog == .confirmContinue },
            set: { if !$0 { dismissDialog() } }
        )
    }

    private var isConfirmBookingPresented: Binding<Bool> {
        Binding<Bool>(
            get: { dialog == .confirmBooking },
            set: { if !$0 { dismissDialog() } }
        )
    }

    private var isChangeExistingPresented: Binding<Bool> {
        Binding<Bool>(
            get: { dialog == .changeExisting },
            set: { if !$0 { dismissDialog() } }
        )
    }

    private var isBookingMenuPresented: Binding<Bool> {
        Binding<Bool>(
            get: { bookingMenuTarget != nil },
            set: { if !$0 { bookingMenuTarget = nil } }
        )
    }

    var body: some View {
        SwiftUI.ScrollView(.vertical) {
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
        .sheet(isPresented: isReservationPresented) {
            reservationSheet
                .presentationDetents([.medium, .large])
        }
        .alert("Aviso", isPresented: isConfirmContinuePresented) {
            Button("No", role: .cancel) { dismissDialog() }
            Button("Sí") { dialog = .reservation }
        } message: {
            Text("Ya tienes una reserva hoy. ¿Continuar?")
        }
        .alert("Confirmar reserva", isPresented: isConfirmBookingPresented) {
            Button("Cancelar", role: .cancel) { dismissDialog() }
            Button("Reservar") { submitBooking() }
            Button("Cambiar") { dialog = .changeExisting }
        } message: {
            Text(confirmBookingMessage)
        }
        .accessibilityIdentifier("calendarView")
        .confirmationDialog(
            "Gestión de reserva",
            isPresented: isChangeExistingPresented,
            titleVisibility: .visible
        ) {
            if let success = bookingsViewModel.userBookings as? FetchUserBookingsStateSuccess {
                ForEach(success.bookings, id: \.id) { booking in
                    let bookingDay = String(booking.date.prefix(10))
                    let bookingHour = String(booking.hour.prefix(5))
                    let title = "Cambiar #\(booking.id) (\(bookingDay) \(bookingHour))"
                    Button(title) {
                        submitBookingChange(booking: booking)
                    }
                }
            }
            Button("Cancelar", role: .cancel) { dismissDialog() }
        }
        .confirmationDialog(
            "Reserva",
            isPresented: isBookingMenuPresented,
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
            ForEach(days.indices, id: \.self) { index in
                dayCell(for: days[index], today: today)
            }
        }
    }

    @ViewBuilder
    private func dayCell(for maybeDate: Date?, today: Date) -> some View {
        if let date = maybeDate {
            let isHoliday = isHolidayDate(date)
            let isReserved = isReservedDate(date)
            let selectable = isSelectable(date: date, today: today, isHoliday: isHoliday)
            let dayText = "\(calendar.component(.day, from: date))"
            let cellBackground = backgroundColor(
                date: date,
                isReserved: isReserved,
                isHoliday: isHoliday,
                selectable: selectable
            )
            let borderColor: Color = calendar.isDateInToday(date) ? .secondary : .clear

            Text(dayText)
                .fontWeight(.bold)
                .frame(maxWidth: .infinity, minHeight: 34)
                .padding(.vertical, 3)
                .background(cellBackground)
                .clipShape(Circle())
                .overlay(Circle().stroke(borderColor, lineWidth: 2))
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

    @ViewBuilder
    private func userBookingsSection(bookings: [UserBooking]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Mis sesiones reservadas")
                .font(.headline)

            let filterValues = ["Todos"] + Array(Set(bookings.map { $0.service })).sorted()
            Picker("Servicio", selection: $bookingsFilter) {
                ForEach(filterValues, id: \.self) { filter in
                    Text(filter).tag(filter as String)
                }
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

            if !userProducts.isEmpty {
                Picker("Servicio", selection: selectedProductBinding) {
                    Text("Seleccionar").tag(Int32(-1))
                    ForEach(userProducts, id: \.id) { product in
                        Text(product.name).tag(productIdInt32(product))
                    }
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
        } else if sessionsStateName.contains("Loading") {
            ProgressView().frame(maxWidth: .infinity)
        } else if sessionsStateName.contains("Success") {
            let hours = availableSessionHours()
            ForEach(hours, id: \.self) { hour in
                Button(String(hour.prefix(5))) {
                    let coaches = daySessionViewModel.getAvailableCoachesForHour(hour: hour)
                    if let coach = coaches.first {
                        selectedHour = hour
                        selectedCoach = coach
                        dialog = .confirmBooking
                    }
                }
                .buttonStyle(.borderedProminent)
            }
        } else if sessionsStateName.contains("Empty") {
            Text("No hay horarios disponibles.")
                .foregroundColor(.secondary)
        } else if let message = sessionsErrorMessage() {
            Text(message).foregroundColor(.red)
        }
    }

    private var selectedProductBinding: Binding<Int32> {
        Binding<Int32>(
            get: { selectedProduct.map(productIdInt32) ?? -1 },
            set: { newId in
                selectedProduct = userProducts.first(where: { productIdInt32($0) == newId })
                guard let product = selectedProduct else { return }
                daySessionViewModel.fetchAvailableSessions(
                    productId: productIdInt32(product),
                    date: kmmDate(from: selectedDate)
                )
            }
        )
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

        let productId = productIdInt32(product)

        daySessionViewModel.fetchServiceIdForProductAsync(productId: productId) { serviceId in
            guard let serviceId, serviceId.int32Value > 0 else { return }
            daySessionViewModel.makeBookingAsync(
                customerId: userId,
                coachId: coach.coachId,
                serviceId: Int32(serviceId),
                productId: productId,
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

        let productId = productIdInt32(product)

        daySessionViewModel.fetchServiceIdForProductAsync(productId: productId) { serviceId in
            guard let serviceId, serviceId.int32Value > 0 else { return }
            daySessionViewModel.modifyBookingSessionAsync(
                bookingId: booking.id,
                newCoachId: coach.coachId,
                newServiceId: Int32(serviceId),
                newProductId: productId,
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
        let year = Int32(calendar.component(.year, from: date))
        let month = Int32(calendar.component(.month, from: date))
        let day = Int32(calendar.component(.day, from: date))

        for holiday in daySessionViewModel.holidays {
            let holidayYear = holiday.year.int32Value
            let holidayMonth = holiday.monthNumber.int32Value
            let holidayDay = holiday.dayOfMonth.int32Value
            if holidayYear == year && holidayMonth == month && holidayDay == day {
                return true
            }
        }

        return false
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

        let isCurrentMonth = monthOfDate == currentMonth && yearOfDate == currentYear
        let isNextMonth = monthOfDate == nextMonth && yearOfDate == nextYear

        if day < 15 {
            return isCurrentMonth
        }

        return isCurrentMonth || (isAfter15 && isNextMonth)
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
            month: Int32(calendar.component(.month, from: date)),
            day: Int32(calendar.component(.day, from: date))
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

    private func productIdInt32(_ product: Product) -> Int32 {
        let rawId = product.id
        if let id = rawId as? Int32 { return id }
        if let id = rawId as? Int { return Int32(id) }
        if let id = mirrorValue(from: rawId, label: "int32Value") as? Int32 { return id }
        return 0
    }

    private func availableSessionHours() -> [String] {
        guard let sessions = mirrorValue(from: daySessionViewModel.sessions, label: "sessions") as? [DaySession] else {
            return []
        }
        return Array(Set(sessions.map { $0.hour })).sorted()
    }

    private func sessionsErrorMessage() -> String? {
        mirrorValue(from: daySessionViewModel.sessions, label: "message") as? String
    }

    private var confirmBookingMessage: String {
        let coachName = selectedCoach?.coachName ?? "-"
        let hour = selectedHour.map { String($0.prefix(5)) } ?? ""
        return "¿Deseas confirmar tu sesión con \(coachName) a las \(hour)?"
    }
}

private enum CalendarDialog {
    case hidden
    case confirmContinue
    case reservation
    case confirmBooking
    case changeExisting
}

private func mirrorValue(from state: Any, label: String) -> Any? {
    Mirror(reflecting: state).children.first(where: { $0.label == label })?.value
}
