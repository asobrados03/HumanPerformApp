//
//  CalendarView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Lista de sesiones disponibles para la fecha seleccionada.
private extension DateFormatter {
    static let yyyymmdd: DateFormatter = {
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd"
        df.locale = Locale(identifier: "en_US_POSIX")
        df.timeZone = TimeZone(secondsFromGMT: 0)
        return df
    }()
}

/// Vista de calendario simple para iOS que replica la funcionalidad de la
/// pantalla equivalente en Android, incluyendo filtros por servicio,
/// comprobación de límites semanales, cuestionarios y notificaciones.
struct CalendarView: View {
    /// Mes actualmente mostrado. Se inicializa con la fecha actual.
    @State private var currentMonth: Date = Date()

    /// Día seleccionado por el usuario.
    @State private var selectedDate: Date = Date()

    /// ViewModel encargado de consultar las sesiones disponibles para cada día.
    @StateViewModel private var daySessionViewModel = makeDaySessionViewModel()

    /// Maneja los datos de sesión del usuario, incluyendo servicios permitidos.
    @StateViewModel private var sessionViewModel = makeUserViewModel()

    /// ViewModel del usuario para acciones sobre reservas.
    @StateViewModel private var userViewModel = makeUserViewModel()

    /// Servicio seleccionado por el usuario para filtrar las sesiones.
    @State private var selectedServiceId: Int32? = nil

    private let calendar = Calendar.current

    var body: some View {
        VStack(spacing: 16) {
            monthHeader
            if !sessionViewModel.allowedServices.isEmpty {
                servicePicker
            }
            calendarGrid
            sessionList
            Spacer()
        }
        .padding(.horizontal)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear {
            sessionViewModel.loadAllowedServicesForUser()
            if let uid = sessionViewModel.userId {
                daySessionViewModel.fetchUserWeeklyLimit(userId: Int32(uid))
                daySessionViewModel.refreshUserBookings(userId: Int32(uid)) {
                    scheduleNotificationsForBookings()
                }
            }
            if let first = sessionViewModel.allowedServices.first {
                selectedServiceId = Int32(first.id)
                daySessionViewModel.fetchSessions(serviceId: Int32(first.id), date: selectedDate)
            } else {
                daySessionViewModel.fetchSessions(serviceId: 1, date: selectedDate)
            }
            daySessionViewModel.fetchHolidays()
        }
        .onChange(of: sessionViewModel.userId) { uid in
            if let id = uid {
                daySessionViewModel.fetchUserWeeklyLimit(userId: Int32(id))
                daySessionViewModel.refreshUserBookings(userId: Int32(id)) {
                    scheduleNotificationsForBookings()
                }
            }
        }
        .onChange(of: sessionViewModel.allowedServices) { services in
            if selectedServiceId == nil, let first = services.first {
                selectedServiceId = Int32(first.id)
                daySessionViewModel.fetchSessions(serviceId: Int32(first.id), date: selectedDate)
            }
        }
        .onChange(of: selectedDate) { newDate in
            if let serviceId = selectedServiceId {
                daySessionViewModel.fetchSessions(serviceId: serviceId, date: newDate)
            }
        }
        .onChange(of: selectedServiceId) { newService in
            if let id = newService {
                daySessionViewModel.fetchSessions(serviceId: id, date: selectedDate)
            }
        }
        .onChange(of: daySessionViewModel.userBookings) { _ in
            scheduleNotificationsForBookings()
        }
        .alert(isPresented: Binding(
            get: { daySessionViewModel.reservationResult != nil },
            set: { _ in daySessionViewModel.reservationResult = nil }
        )) {
            let title: String
            switch daySessionViewModel.reservationResult {
            case "success":
                title = "Reserva confirmada"
            case "updated":
                title = "Reserva actualizada"
            case "limit":
                title = "Límite semanal alcanzado"
            default:
                title = "Error"
            }
            return Alert(title: Text(title), dismissButton: .default(Text("OK")))
        }
        .sheet(isPresented: $daySessionViewModel.questionnaireActive) {
            questionnaireSheet
        }
    }

    /// Cabecera con el nombre del mes y botones para navegar.
    private var monthHeader: some View {
        HStack {
            Button(action: previousMonth) {
                Image(systemName: "chevron.left")
            }
            Spacer()
            Text(monthYearString(currentMonth))
                .font(.title2)
                .bold()
            Spacer()
            Button(action: nextMonth) {
                Image(systemName: "chevron.right")
            }
        }
    }

    /// Rejilla con los días del mes.
    private var calendarGrid: some View {
        let days = generateDays(for: currentMonth)
        let columns = Array(repeating: GridItem(.flexible()), count: 7)
        return LazyVGrid(columns: columns, spacing: 8) {
            ForEach(["L","M","X","J","V","S","D"], id: \.self) { weekday in
                Text(weekday)
                    .font(.caption)
                    .frame(maxWidth: .infinity)
            }
            ForEach(Array(days.enumerated()), id: \.offset) { _, date in
                if let date = date {
                    let isHoliday = daySessionViewModel.holidays.contains { calendar.isDate($0, inSameDayAs: date) }
                    dayView(date, isHoliday: isHoliday)
                } else {
                    Text("")
                        .frame(maxWidth: .infinity, minHeight: 32)
                }
            }
        }
    }

    

    private var sessionList: some View {
        // Fuera del ViewBuilder puedes tener sentencias normales
        let selectedDay = DateFormatter.yyyymmdd.string(from: selectedDate)

        return VStack(alignment: .leading, spacing: 8) {
            if daySessionViewModel.sessions.isEmpty {
                Text("No hay sesiones disponibles")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            } else {
                ForEach(daySessionViewModel.sessions, id: \.hour) { session in
                    // Solo expresiones (let) dentro del ViewBuilder
                    let sHour = session.hour.count == 5 ? session.hour : String(session.hour.prefix(5))

                    let bookingForSession = daySessionViewModel.userBookings.first { b in
                        let bDay  = String(b.date.prefix(10))
                        let bHour = b.hour.count == 5 ? b.hour : String(b.hour.prefix(5))
                        return bDay == selectedDay && bHour == sHour && b.coach_name == session.coachName
                    }

                    let bookingSameDay = daySessionViewModel.userBookings.first {
                        String($0.date.prefix(10)) == selectedDay
                    }

                    sessionRow(
                        session: session,
                        bookingForSession: bookingForSession,
                        bookingSameDay: bookingSameDay
                    )
                }
            }
        }
    }


    /// Vista para cada fila de sesión. Se extrae para reducir la complejidad del cuerpo y
    /// evitar que el compilador tarde demasiado en verificar tipos.
    @ViewBuilder
    private func sessionRow(session: DaySession,
                            bookingForSession: UserBooking?,
                            bookingSameDay: UserBooking?) -> some View {
        HStack {
            VStack(alignment: .leading) {
                Text(session.hour)
                    .font(.body)
                Text(session.coachName ?? "-")
                    .font(.caption)
            }
            Spacer()
            Text("\(session.booked)/\(session.capacity)")
                .font(.caption)
                .foregroundColor(.secondary)
            if let userId = sessionViewModel.userId, let serviceId = selectedServiceId {
                if bookingForSession != nil {
                    Text("Reservado")
                        .font(.caption)
                        .foregroundColor(.green)
                } else if let existing = bookingSameDay {
                    Button("Cambiar") {
                        daySessionViewModel.updateReservation(
                            customerId: Int32(userId),
                            bookingId: Int32(existing.id),
                            newCoachId: Int32(session.coachId),
                            newServiceId: serviceId,
                            date: selectedDate,
                            hour: session.hour
                        )
                    }
                    .buttonStyle(.borderedProminent)
                } else {
                    Button("Reservar") {
                        daySessionViewModel.reserveSession(
                            customerId: Int32(userId),
                            coachId: Int32(session.coachId),
                            serviceId: serviceId,
                            centerId: 1,
                            date: selectedDate,
                            hour: session.hour
                        )
                    }
                    .buttonStyle(.borderedProminent)
                }
            }
        }
        .padding(8)
        .background(Color.gray.opacity(0.1))
        .cornerRadius(8)
        .contextMenu {
            if let booking = bookingForSession {
                Button("Descargar evento") {
                    let hour = booking.hour.count == 5 ? booking.hour + ":00" : booking.hour
                    let iso = String(booking.date.prefix(10)) + "T" + hour
                    let formatter = ISO8601DateFormatter()
                    if let date = formatter.date(from: iso) {
                        let ics = createICSFile(eventTitle: booking.service, startDate: date)
                        shareICS(content: ics)
                    }
                }
                Button(role: .destructive) {
                    userViewModel.cancelUserBooking(bookingId: Int32(booking.id)) {
                        if let uid = sessionViewModel.userId {
                            daySessionViewModel.refreshUserBookings(userId: Int32(uid)) {
                                scheduleNotificationsForBookings()
                            }
                        }
                    }
                } label: {
                    Text("Cancelar reserva")
                }
            }
        }
    }

    /// Vista individual para cada día.
    private func dayView(_ date: Date, isHoliday: Bool) -> some View {
        let isSelected = calendar.isDate(date, inSameDayAs: selectedDate)
        return Text("\(calendar.component(.day, from: date))")
            .frame(maxWidth: .infinity, minHeight: 32)
            .background(isSelected ? Color.blue.opacity(0.2) : Color.clear)
            .clipShape(Circle())
            .foregroundColor(isHoliday ? .red : .primary)
            .onTapGesture { if !isHoliday { selectedDate = date } }
            .disabled(isHoliday)
    }

    /// Programa notificaciones para todas las reservas del usuario.
    private func scheduleNotificationsForBookings() {
        let formatter = ISO8601DateFormatter()
        for booking in daySessionViewModel.userBookings {
            let hour = booking.hour.count == 5 ? booking.hour + ":00" : booking.hour
            let iso = String(booking.date.prefix(10)) + "T" + hour
            if let date = formatter.date(from: iso) {
                daySessionViewModel.scheduleNotification(
                    bookingId: Int32(booking.id),
                    hour: hour,
                    date: date
                )
            }
        }
    }

    /// Genera los días a mostrar para un mes determinado, incluyendo los huecos
    /// iniciales necesarios para alinear la primera semana con el día correcto.
    private func generateDays(for date: Date) -> [Date?] {
        var days: [Date?] = []
        let startOfMonth = calendar.date(from: calendar.dateComponents([.year, .month], from: date))!

        var firstWeekday = calendar.component(.weekday, from: startOfMonth)
        firstWeekday = firstWeekday == 1 ? 7 : firstWeekday - 1

        for _ in 1..<firstWeekday {
            days.append(nil)
        }

        let range = calendar.range(of: .day, in: .month, for: startOfMonth)!
        for day in range {
            days.append(calendar.date(byAdding: .day, value: day - 1, to: startOfMonth))
        }

        return days
    }

    /// Devuelve una cadena con el nombre del mes y año en español.
    private func monthYearString(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_ES")
        formatter.dateFormat = "LLLL yyyy"
        return formatter.string(from: date).capitalized
    }

    /// Navega al mes anterior.
    private func previousMonth() {
        currentMonth = calendar.date(byAdding: .month, value: -1, to: currentMonth) ?? currentMonth
    }

    /// Navega al mes siguiente.
    private func nextMonth() {
        currentMonth = calendar.date(byAdding: .month, value: 1, to: currentMonth) ?? currentMonth
    }

    /// Selector de servicio que permite filtrar las sesiones disponibles.
    private var servicePicker: some View {
        Picker("Servicio", selection: Binding(
            get: { selectedServiceId ?? Int32(0) },
            set: { selectedServiceId = $0 }
        )) {
            ForEach(sessionViewModel.allowedServices, id: \.id) { service in
                Text(service.name).tag(Int32(service.id))
            }
        }
        .pickerStyle(SegmentedPickerStyle())
    }

    /// Vista del cuestionario previo a la sesión.
    private var questionnaireSheet: some View {
        let questions = [
            "¿Cómo dormiste?",
            "¿Nivel de energía?",
            "¿Dolor muscular?",
            "¿Nivel de estrés?",
            "¿Estado de ánimo?"
        ]

        return VStack(spacing: 16) {
            Text(questions[daySessionViewModel.currentQuestion])
                .font(.headline)
            HStack {
                ForEach(1...5, id: \.self) { value in
                    Button("\(value)") {
                        daySessionViewModel.answerQuestion("\(value)")
                    }
                    .buttonStyle(.bordered)
                }
            }
            Button("Omitir") {
                daySessionViewModel.skipQuestionnaire()
            }
            .padding(.top, 8)
        }
        .padding()
    }
}

