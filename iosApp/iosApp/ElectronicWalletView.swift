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
    @EnvironmentObject var userVM: UserViewModel
    @State private var showDetails = false

    var body: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading, spacing: 12) {
                Text("💳 Saldo actual: \(userVM.balance, specifier: "%.2f") €")
                    .fontWeight(.semibold)

                Text(showDetails ? "🔼 Ocultar detalles" : "▶️ Ver detalles")
                    .fontWeight(.medium)
                    .foregroundColor(.blue)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .onTapGesture { showDetails.toggle() }

                if showDetails {
                    VStack(alignment: .leading, spacing: 8) {
                        ForEach(Array(userVM.ewalletTransactions.enumerated()), id: \.offset) { _, tx in
                            VStack(alignment: .leading, spacing: 4) {
                                let fecha = String(tx.date.prefix(10))
                                Text("📅 \(fecha)")
                                Text("💰 \(tx.amount > 0 ? "+" : "")\(tx.amount, specifier: "%.2f") €")
                                Text("📝 \(tx.description)")
                            }
                            .padding(.vertical, 8)
                        }
                    }
                }
            }
            .padding(16)
            .background(Color(.systemBackground))
            .cornerRadius(8)
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

