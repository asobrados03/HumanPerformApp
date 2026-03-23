//
//  StatsView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct StatsView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var statsViewModel = SharedDependencies.shared.makeUserStatsViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                switch statsViewModel.uiState {
                case is UserStatsStateLoading:
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)

                case let error as UserStatsStateError:
                    VStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.red)
                        Text("Error: \(error.message)")
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                        Button(action: loadStats) {
                            Label("Reintentar", systemImage: "arrow.clockwise")
                        }
                    }
                    .frame(maxWidth: .infinity)

                case let success as UserStatsStateSuccess:
                    Text("📊 Tus estadísticas")
                        .font(.title3)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    let workouts = Int(success.stats.lastMonthWorkouts)
                    StatCard(
                        title: "📅 Entrenamientos del mes pasado",
                        value: workouts == 1 ? "1 sesión" : "\(workouts) sesiones"
                    )

                    StatCard(
                        title: "🏋️ Entrenador más usado",
                        value: success.stats.mostFrequentTrainer ?? "No hay datos disponibles"
                    )

                    let bookings = Int(success.stats.pendingBookings)
                    StatCard(
                        title: "⏳ Reservas pendientes",
                        value: bookings == 1 ? "1 sesión pendiente" : "\(bookings) sesiones pendientes"
                    )

                default:
                    EmptyView()
                }
            }
            .padding(16)
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear(perform: loadStats)
        .onChange(of: sessionVM.userData?.id) { _ in loadStats() }
    }

    private func loadStats() {
        if let id = sessionVM.userData?.id {
            statsViewModel.loadStatistics(userId: id)
        }
    }
}

private struct StatCard: View {
    let title: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .fontWeight(.semibold)
            Text(value)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(UIColor.secondarySystemBackground))
        )
    }
}
