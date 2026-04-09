import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct MyProductsView: View {
    @StateViewModel var serviceProductViewModel: shared.ServiceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserSessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel var stripeViewModel: shared.StripeViewModel = SharedDependencies.shared.makeStripeViewModel()
    @State private var selectedProduct: Product? = nil
    @State private var showProductOptions = false
    @State private var showUnsubscribeConfirm = false
    @State private var pendingRefundProductId: Int32? = nil
    @State private var successMessage: String? = nil
    @State private var errorMessage: String? = nil

    var onOpenProductDetail: (Int) -> Void = { _ in }

    private var productOptionsTitle: String {
        if let product = selectedProduct {
            return "Producto: \(product.name)"
        }
        return ""
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                switch serviceProductViewModel.userProductsStateKind() {
                case "loading":
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                case "success":
                    let products = serviceProductViewModel.userProductsStateProducts()
                    if products.isEmpty {
                        Text("No tienes productos contratados.")
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(products, id: \.id) { producto in
                            ProductRow(producto: producto)
                                .onTapGesture {
                                    selectedProduct = producto
                                    showProductOptions = true
                                }
                        }
                    }
                case "error":
                    VStack(spacing: 8) {
                        Text(serviceProductViewModel.userProductsStateMessage() ?? "Error desconocido")
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)

                        if let id = sessionViewModel.userData?.id {
                            Button("Reintentar") {
                                serviceProductViewModel.loadUserProducts(userId: id)
                            }
                        }
                    }
                default:
                    EmptyView()
                }
            }
            .padding(12)
        }
        .onAppear {
            if let id = sessionViewModel.userData?.id {
                serviceProductViewModel.loadUserProducts(userId: id)
            }
        }
        .onChange(of: stripeViewModel.actionStateKind()) { newKind in
            guard let userId = sessionViewModel.userData?.id else { return }

            switch newKind {
            case "success":
                serviceProductViewModel.loadUserProducts(userId: userId)
                successMessage = "Suscripción cancelada correctamente."
                stripeViewModel.resetActionState()
            case "error":
                errorMessage = stripeViewModel.actionStateMessage() ?? "No se pudo cancelar la suscripción."
                stripeViewModel.resetActionState()
            default:
                break
            }
        }
        .onChange(of: stripeViewModel.refundStateKind()) { newKind in
            guard let userId = sessionViewModel.userData?.id else { return }

            switch newKind {
            case "success":
                if let refundedProductId = stripeViewModel.refundStateProductId(),
                   let pendingRefundProductId,
                   refundedProductId.int32Value == pendingRefundProductId {
                    serviceProductViewModel.unassignProductFromUser(productId: pendingRefundProductId, userId: userId)
                }
                pendingRefundProductId = nil
                successMessage = "Reembolso completado. Producto dado de baja."
                stripeViewModel.resetRefundState()
            case "error":
                errorMessage = stripeViewModel.refundStateMessage() ?? "No se pudo completar el reembolso."
                pendingRefundProductId = nil
                stripeViewModel.resetRefundState()
            default:
                break
            }
        }
        .onChange(of: sessionViewModel.userData?.id) { newId in
            if let id = newId {
                serviceProductViewModel.loadUserProducts(userId: id)
            }
        }
        .confirmationDialog(
            productOptionsTitle,
            isPresented: $showProductOptions,
            titleVisibility: .visible
        ) {
            Button("Ver detalles") {
                if let productId = selectedProduct?.id {
                    onOpenProductDetail(Int(productId))
                }
            }
            Button("Darse de baja", role: .destructive) {
                showUnsubscribeConfirm = true
            }
            Button("Cancelar", role: .cancel) { }
        } message: {
            Text("¿Qué deseas hacer con este producto?")
        }
        .alert("Confirmar baja", isPresented: $showUnsubscribeConfirm) {
            Button("Cancelar", role: .cancel) { }
            Button("Sí, darse de baja", role: .destructive) {
                if let prod = selectedProduct, let userId = sessionViewModel.userData?.id {
                    if let subscriptionId = prod.stripeSubscriptionId, !subscriptionId.isEmpty {
                        stripeViewModel.cancelSubscription(
                            subscriptionId: subscriptionId,
                            productId: prod.id,
                            userId: userId
                        )
                    } else if let paymentIntentId = prod.stripePaymentIntentId, !paymentIntentId.isEmpty {
                        pendingRefundProductId = prod.id
                        stripeViewModel.createRefund(
                            paymentIntentId: paymentIntentId,
                            productId: prod.id,
                            amount: prod.price
                        )
                    } else {
                        serviceProductViewModel.unassignProductFromUser(productId: prod.id, userId: userId)
                        successMessage = "Producto dado de baja correctamente."
                    }
                }
                selectedProduct = nil
            }
        } message: {
            Text("¿Estás seguro de que quieres darte de baja de este producto?")
        }
        .alert("Operación completada", isPresented: Binding(
            get: { successMessage != nil },
            set: { if !$0 { successMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(successMessage ?? "")
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
}
