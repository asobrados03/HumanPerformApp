//
//  StatsView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Updated by ChatGPT on 2025-08-28.
//

import SwiftUI
import shared

struct StatsView: View {
    @StateObject private var userViewModel = UserViewModel()
    @StateObject private var statsViewModel = StatsViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if statsViewModel.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                } else if let error = statsViewModel.error {
                    VStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.red)
                        Text("Error: \(error)")
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                        Button(action: loadStats) {
                            Label("Reintentar", systemImage: "arrow.clockwise")
                        }
                    }
                    .frame(maxWidth: .infinity)
                } else {
                    Text("📊 Tus estadísticas")
                        .font(.title3)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    StatCard(title: "📅 Entrenamientos del mes pasado",
                             value: "\(statsViewModel.entrenamientosMesPasado) sesiones")
                    StatCard(title: "🏋️ Entrenador más usado",
                             value: statsViewModel.entrenadorMasUsado ?? "No hay datos disponibles")
                    StatCard(title: "⏳ Reservas pendientes",
                             value: "\(statsViewModel.reservasPendientes) sesiones pendientes")
                }
            }
            .padding(16)
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear(perform: loadStats)
        .onChange(of: userViewModel.currentUserId) { _ in loadStats() }
    }

    private func loadStats() {
        if let id = userViewModel.currentUserId {
            statsViewModel.loadStats(userId: id)
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
