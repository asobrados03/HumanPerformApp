//
//  PaymentMethodsView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

extension StripePaymentMethod: Identifiable {}

/// Pantalla para visualizar los métodos de pago del usuario.
struct PaymentMethodsView: View {
    @StateViewModel private var vm = SharedDependencies.shared.makeStripeViewModel()

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Tus métodos de pago")
                .font(.title2)
                .fontWeight(.semibold)
                .padding(.horizontal, 20)
                .padding(.vertical, 12)

            content
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear {
            vm.loadPaymentMethods()
        }
    }

    @ViewBuilder
    private var content: some View {
        switch vm.viewPaymentMethodsUiState {
        case is PaymentMethodsUiStateLoading:
            PaymentMethodsShimmerView()
        case let error as PaymentMethodsUiStateError:
            ErrorStateView(message: error.message) { vm.loadPaymentMethods() }
        case is PaymentMethodsUiStateEmpty:
            EmptyStateView(
                title: "No hay métodos todavía",
                subtitle: "Añade tu primera tarjeta para pagar más rápido."
            )
        case let success as PaymentMethodsUiStateSuccess:
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(success.paymentMethods) { pm in
                        PaymentMethodCard(
                            paymentMethod: pm,
                            isDefault: pm.id == success.defaultPaymentMethodId
                        )
                    }
                    Spacer().frame(height: 32)
                }
                .padding(.horizontal, 16)
            }
        default:
            EmptyStateView(
                title: "No hay métodos todavía",
                subtitle: "Añade tu primera tarjeta para pagar más rápido."
            )
        }
    }
}

/* --------- Componentes --------- */

struct PaymentMethodCard: View {
    let paymentMethod: StripePaymentMethod
    let isDefault: Bool

    private var brand: String {
        paymentMethod.card.brand.uppercased()
    }

    private var last4: String {
        paymentMethod.card.last4
    }

    private var exp: String {
        String(format: "%02d/%02d", paymentMethod.card.expMonth, paymentMethod.card.expYear % 100)
    }

    var body: some View {
        HStack(alignment: .center) {
            BrandAvatar(brand: brand)
            VStack(alignment: .leading, spacing: 4) {
                Text(brand)
                    .font(.headline)
                Text("•••• \(last4)  ·  \(exp)")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            Spacer()
            if isDefault {
                DefaultChip()
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(Color(.systemBackground))
                .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: 1)
        )
    }
}

struct BrandAvatar: View {
    let brand: String

    var body: some View {
        ZStack {
            Circle()
                .fill(Color.secondary.opacity(0.2))
            HStack(spacing: 4) {
                Image(systemName: "creditcard")
                    .font(.system(size: 18))
                Text(String(brand.prefix(2)))
                    .font(.system(size: 14, weight: .bold))
            }
        }
        .frame(width: 44, height: 44)
    }
}

struct DefaultChip: View {
    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 16))
                .foregroundColor(Color.accentColor)
            Text("Predeterminado")
                .font(.caption)
                .foregroundColor(Color.accentColor)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(
            Capsule().fill(Color.accentColor.opacity(0.12))
        )
    }
}

struct EmptyStateView: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "creditcard")
                .font(.system(size: 64))
                .foregroundColor(.accentColor)
            Text(title)
                .font(.title3)
            Text(subtitle)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

struct ErrorStateView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 56))
                .foregroundColor(.red)
            Text("No se pudo cargar")
                .font(.title3)
            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            Button("Reintentar", action: onRetry)
                .padding(.top, 16)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}

struct PaymentMethodsShimmerView: View {
    var body: some View {
        VStack(spacing: 12) {
            ForEach(0..<4, id: \.self) { _ in
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.gray.opacity(0.3))
                    .frame(height: 82)
                    .redacted(reason: .placeholder)
                    .shimmer()
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }
}

private struct Shimmer: ViewModifier {
    @State private var phase: CGFloat = -0.7

    func body(content: Content) -> some View {
        content
            .overlay(
                GeometryReader { geometry in
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.gray.opacity(0.3),
                            Color.gray.opacity(0.1),
                            Color.gray.opacity(0.3)
                        ]),
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                    .frame(width: geometry.size.width * 1.5, height: geometry.size.height)
                    .rotationEffect(.degrees(30))
                    .offset(x: geometry.size.width * phase)
                }
            )
            .mask(content)
            .onAppear {
                withAnimation(.linear(duration: 1.2).repeatForever(autoreverses: false)) {
                    phase = 0.7
                }
            }
    }
}

extension View {
    func shimmer() -> some View {
        modifier(Shimmer())
    }
}
