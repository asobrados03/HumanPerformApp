import SwiftUI
import KMPObservableViewModelSwiftUI
import shared
import StripePaymentSheet

extension StripePaymentMethod: Identifiable {}

struct PaymentMethodsView: View {
    @StateViewModel private var stripeVM = SharedDependencies.shared.makeStripeViewModel()
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()

    @State private var paymentSheet: PaymentSheet?
    @State private var isPresentingSetupSheet = false
    @State private var infoMessage: String?
    @State private var errorMessage: String?
    @State private var pendingDeleteId: String?

    private var methodsStateKind: String { stripeVM.paymentMethodsStateKind() }
    private var addStateKind: String { stripeVM.addPaymentMethodStateKind() }
    private var actionStateKind: String { stripeVM.actionStateKind() }

    var body: some View {
        ZStack {
            VStack(alignment: .leading, spacing: 0) {
                Text("Tus métodos de pago")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)

                content
            }

            if addStateKind == "loading" || actionStateKind == "loading" {
                Color.black.opacity(0.15).ignoresSafeArea()
                ProgressView()
                    .padding(20)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 14))
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear { stripeVM.loadPaymentMethods() }
        .onChange(of: addStateKind) { _ in handleAddPaymentStateChange() }
        .onChange(of: actionStateKind) { _ in handleActionStateChange() }
        .onChange(of: methodsStateKind) { _ in
            if methodsStateKind == "error" {
                errorMessage = stripeVM.paymentMethodsStateMessage() ?? "No se pudieron cargar los métodos de pago"
            }
        }
        .paymentSheet(isPresented: $isPresentingSetupSheet, paymentSheet: paymentSheet) { result in
            switch result {
            case .completed:
                stripeVM.onAddPaymentMethodCompleted()
            case .canceled:
                stripeVM.onAddPaymentMethodCanceled()
            case .failed(let error):
                stripeVM.onAddPaymentMethodFailed(message: error.localizedDescription)
            }
            paymentSheet = nil
        }
        .alert("Eliminar método", isPresented: Binding(
            get: { pendingDeleteId != nil },
            set: { if !$0 { pendingDeleteId = nil } }
        )) {
            Button("Cancelar", role: .cancel) { pendingDeleteId = nil }
            Button("Eliminar", role: .destructive) {
                if let pendingDeleteId {
                    stripeVM.detachPaymentMethod(paymentMethodId: pendingDeleteId)
                }
                self.pendingDeleteId = nil
            }
        } message: {
            Text("¿Seguro que deseas eliminar este método de pago?")
        }
        .alert("Éxito", isPresented: Binding(
            get: { infoMessage != nil },
            set: { if !$0 { infoMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(infoMessage ?? "")
        }
        .alert("Error", isPresented: Binding(
            get: { errorMessage != nil },
            set: { if !$0 { errorMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "")
        }
    }

    @ViewBuilder
    private var content: some View {
        if methodsStateKind == "loading" {
            PaymentMethodsShimmerView()
        } else if methodsStateKind == "error" {
            ErrorStateView(message: stripeVM.paymentMethodsStateMessage() ?? "Error desconocido") {
                stripeVM.loadPaymentMethods()
            }
        } else if methodsStateKind == "empty" {
            VStack(spacing: 16) {
                EmptyStateView(
                    title: "No hay métodos todavía",
                    subtitle: "Añade tu primera tarjeta para pagar más rápido."
                )
                addButton(fullWidth: false)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.horizontal, 24)
        } else {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(stripeVM.paymentMethodsList(), id: \.id) { pm in
                        PaymentMethodCard(
                            paymentMethod: pm,
                            isDefault: pm.id == stripeVM.paymentMethodsDefaultId(),
                            onDelete: { pendingDeleteId = pm.id },
                            onSetDefault: { stripeVM.setDefaultPaymentMethod(paymentMethodId: pm.id) }
                        )
                    }

                    addButton(fullWidth: true)
                        .padding(.top, 6)

                    Spacer().frame(height: 20)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 12)
            }
        }
    }

    private func addButton(fullWidth: Bool) -> some View {
        Button {
            guard let userId = sessionVM.userData?.id else {
                errorMessage = "Debes iniciar sesión para añadir una tarjeta"
                return
            }
            stripeVM.prepareAddPaymentMethod(userId: userId)
        } label: {
            HStack(spacing: 8) {
                Image(systemName: "plus.circle")
                Text("Añadir método de pago")
            }
            .frame(maxWidth: fullWidth ? .infinity : nil)
        }
        .buttonStyle(.borderedProminent)
    }

    private func handleAddPaymentStateChange() {
        switch addStateKind {
        case "ready":
            guard let sheetData = stripeVM.addPaymentMethodSheetData() else { return }
            STPAPIClient.shared.publishableKey = sheetData.publishableKey

            var config = PaymentSheet.Configuration()
            config.merchantDisplayName = sheetData.merchantDisplayName
            config.customer = .init(id: sheetData.customerId, ephemeralKeySecret: sheetData.ephemeralKeySecret)
            config.allowsDelayedPaymentMethods = false

            paymentSheet = PaymentSheet(
                setupIntentClientSecret: sheetData.setupIntentClientSecret,
                configuration: config
            )
            isPresentingSetupSheet = true

        case "completed":
            stripeVM.resetAddPaymentMethodState()
            infoMessage = "Tarjeta guardada correctamente"

        case "canceled":
            stripeVM.resetAddPaymentMethodState()
            infoMessage = "Operación cancelada"

        case "failed":
            errorMessage = stripeVM.addPaymentMethodStateMessage() ?? "Error al guardar la tarjeta"
            stripeVM.resetAddPaymentMethodState()

        default:
            break
        }
    }

    private func handleActionStateChange() {
        switch actionStateKind {
        case "success":
            infoMessage = "Operación realizada con éxito"
            stripeVM.resetActionState()
        case "error":
            errorMessage = stripeVM.actionStateMessage() ?? "Error en la operación"
            stripeVM.resetActionState()
        default:
            break
        }
    }
}

struct PaymentMethodCard: View {
    let paymentMethod: StripePaymentMethod
    let isDefault: Bool
    let onDelete: () -> Void
    let onSetDefault: () -> Void

    private var brand: String {
        paymentMethod.card.brand.uppercased()
    }

    private var last4: String {
        paymentMethod.card.last4
    }

    private var exp: String {
        String(format: "%02d/%02d", Int(paymentMethod.card.expMonth), Int(paymentMethod.card.expYear) % 100)
    }

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
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

            Menu {
                if !isDefault {
                    Button("Establecer como predeterminada") { onSetDefault() }
                }
                Button("Eliminar", role: .destructive) { onDelete() }
            } label: {
                Image(systemName: "ellipsis.circle")
                    .font(.title3)
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
            Text("Predeterminada")
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
