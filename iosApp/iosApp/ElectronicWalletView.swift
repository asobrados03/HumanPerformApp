//
//  ElectronicWalletView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Pantalla del monedero virtual del usuario.
struct ElectronicWalletView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var walletVM = SharedDependencies.shared.makeUserWalletViewModel()
    @State private var showDetails = false

    private var balanceState: WalletBalanceUiState {
        walletVM.walletBalanceUiState
    }

    private var walletState: EwalletUiState {
        walletVM.eWalletTransactions
    }

    var body: some View {
        VStack(spacing: 16) {
            VStack(alignment: .leading) {
                Text("Monedero Virtual").font(.title2).fontWeight(.semibold)

                balanceContent

                Button(showDetails ? "Ocultar detalles" : "Ver detalles") { showDetails.toggle() }
                    .buttonStyle(.plain)
                    .foregroundColor(.blue)

                if showDetails {
                    walletDetails
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
            loadWalletData()
        }
        .onChange(of: sessionVM.userData?.id) { _ in
            loadWalletData()
        }
    }

    private func loadWalletData() {
        if let id = sessionVM.userData?.id {
            walletVM.loadBalance(userId: id)
            walletVM.loadEwalletTransactions(userId: id)
        }
    }

    private func retryBalanceLoad() {
        if let id = sessionVM.userData?.id {
            walletVM.loadBalance(userId: id)
        }
    }

    @ViewBuilder
    private var balanceContent: some View {
        switch balanceState {
        case is WalletBalanceUiStateLoading:
            HStack(spacing: 8) {
                ProgressView()
                Text("Cargando saldo…")
            }
            .fontWeight(.semibold)

        case let success as WalletBalanceUiStateSuccess:
            Text("💳 Saldo actual: \(success.amount, format: .currency(code: "EUR"))")
                .fontWeight(.semibold)

        case let error as WalletBalanceUiStateError:
            VStack(alignment: .leading, spacing: 6) {
                Text("💳 Saldo actual: —")
                    .fontWeight(.semibold)
                Text(error.message)
                    .foregroundStyle(.red)
                    .font(.caption)
                Button("Reintentar") {
                    retryBalanceLoad()
                }
                .font(.caption.weight(.semibold))
            }

        default:
            Text("💳 Saldo actual: —")
                .fontWeight(.semibold)
        }
    }

    @ViewBuilder
    private var walletDetails: some View {
        switch walletState {
        case is EwalletUiStateLoading:
            ProgressView()
                .frame(maxWidth: .infinity)
        case let success as EwalletUiStateSuccess:
            List {
                ForEach(Array(success.transactions.enumerated()), id: \.offset) { _, tx in
                    TxRow(tx: tx)
                }
            }
            .listStyle(.plain)
        case let error as EwalletUiStateError:
            Text(error.message)
                .foregroundStyle(.red)
        default:
            EmptyView()
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
