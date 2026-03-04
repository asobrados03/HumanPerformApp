//
//  ElectronicWalletView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Pantalla del monedero virtual del usuario.
struct ElectronicWalletView: View {
    @EnvironmentObject var userVM: shared.UserViewModel
    @State private var showDetails = false

    var body: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading) {
                Text("Monedero Virtual").font(.title2).fontWeight(.semibold)
                Text("💳 Saldo actual: \(userVM.balance, format: .currency(code: "EUR"))")
                    .fontWeight(.semibold)

                Button(showDetails ? "Ocultar detalles" : "Ver detalles") { showDetails.toggle() }
                    .buttonStyle(.plain)
                    .foregroundColor(.blue)

                if showDetails {
                    List {
                        ForEach(Array(userVM.ewalletTransactions.enumerated()), id: \.offset) { _, tx in
                            TxRow(tx: tx)
                        }
                    }
                    .listStyle(.plain)
                    .frame(maxHeight: 420)
                }
            }
            .padding(16)
            .background(.background)
            .cornerRadius(12)
            .shadow(radius: 4)

            Spacer()
        }
        .padding()
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear {
            if let id = userVM.currentUser?.id {
                userVM.loadBalance(for: id)
                userVM.loadEwalletTransactions(for: id)
            }
        }
    }
}

private struct TxRow: View {
    let tx: EwalletTransaction
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(formatDate(tx.date)).foregroundStyle(.secondary)
                Spacer()
                Text(amountString(tx.amount))
                    .fontWeight(.semibold)
                    .foregroundColor(tx.amount >= 0 ? .green : .red)
            }
            // Usa 'tx.description_' si no renombraste en Kotlin
            Text("📝 \(tx.description_)")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 6)
    }
}

private func amountString(_ value: Double) -> String {
    value.formatted(.currency(code: "EUR"))
}
private func formatDate(_ iso: String) -> String {
    if let d = ISO8601DateFormatter().date(from: iso) {
        return d.formatted(date: .abbreviated, time: .omitted)
    }
    return String(iso.prefix(10))
}
