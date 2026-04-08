import SwiftUI
import shared
import KMPObservableViewModelSwiftUI
import StripePaymentSheet

struct ProductDetailView: View {
    let productId: Int
    var onPaymentSuccess: () -> Void = {}

    @StateViewModel private var serviceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel private var sessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()

    @State private var showPaymentOptions = false
    @State private var showWalletConfirm = false
    @State private var openStripeCheckout = false

    private func productImageURL(from imageName: String?) -> URL? {
        guard let imageName,
              let encoded = imageName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
        else {
            return nil
        }

        return URL(string: "\(HttpClientProviderKt.API_BASE_URL)/product_images/\(encoded)")
    }

    private var userCoupons: [Coupon] {
        serviceProductViewModel.userCouponsList()
    }

    private func isAlreadyHired(productId: Int32) -> Bool {
        serviceProductViewModel
            .userProductsStateProducts()
            .contains(where: { $0.id == productId })
    }

    var body: some View {
        Group {
            let stateKind = serviceProductViewModel.productDetailStateKind()

            if stateKind == "loading" || stateKind == "idle" {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if stateKind == "error" {
                if let message = serviceProductViewModel.productDetailStateMessage() {
                    VStack(spacing: 8) {
                        Text(message).foregroundColor(.red)
                        Button("Reintentar") { serviceProductViewModel.loadProductDetail(productId: Int32(productId)) }
                    }
                } else {
                    EmptyView()
                }
            } else if let product = serviceProductViewModel.productDetailStateProduct() {
                detailContent(product: product)
            } else {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationTitle("Detalle")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            serviceProductViewModel.loadProductDetail(productId: Int32(productId))
            loadUserRelatedDataIfPossible()
        }
        .onChange(of: sessionViewModel.userData?.id) { _ in
            loadUserRelatedDataIfPossible()
        }
    }

    private func loadUserRelatedDataIfPossible() {
        guard let userId = sessionViewModel.userData?.id else { return }
        serviceProductViewModel.loadUserCoupons(userId: userId)
        serviceProductViewModel.loadUserProducts(userId: userId)
    }

    @ViewBuilder
    private func detailContent(product: Product) -> some View {
        let alreadyHired = isAlreadyHired(productId: product.id)

        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let productImageURL = productImageURL(from: product.image) {
                    AsyncImage(url: productImageURL) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.gray.opacity(0.2)
                    }
                    .frame(height: 220)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Text(product.name).font(.title3).fontWeight(.bold)
                Text(product.description_ ?? "No hay descripción disponible.")
                    .foregroundColor(.secondary)

                Text("Precio: \(String(format: "%.2f", discountedPrice(for: product)))€")
                    .fontWeight(.bold)
                    .foregroundColor(.red)

                if alreadyHired {
                    Text("Ya has contratado este producto.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                Button(alreadyHired ? "Producto adquirido" : "Comprar") {
                    showPaymentOptions = true
                }
                .buttonStyle(.borderedProminent)
                .disabled(alreadyHired)
            }
            .padding(16)
        }
        .confirmationDialog("Selecciona método de pago", isPresented: $showPaymentOptions) {
            Button("Pagar con Stripe") {
                openStripeCheckout = true
            }

            Button("Usar saldo de la cartera virtual") {
                showWalletConfirm = true
            }
            Button("Cancelar", role: .cancel) {}
        }
        .alert("Confirmar pago", isPresented: $showWalletConfirm) {
            Button("Cancelar", role: .cancel) {}
            Button("Confirmar") {
                assignWithWallet(product: product)
                onPaymentSuccess()
            }
        } message: {
            Text("¿Pagar con saldo virtual?")
        }
        .navigationDestination(isPresented: $openStripeCheckout) {
            StripeCheckoutView(
                product: product,
                couponCode: bestCouponCode(for: product),
                onSuccess: onPaymentSuccess
            )
        }
    }

    private func assignWithWallet(product: Product) {
        guard let userId = sessionViewModel.userData?.id else { return }
        serviceProductViewModel.assignProductToUser(
            userId: userId,
            productId: product.id,
            paymentMethod: "cash",
            couponCode: bestCouponCode(for: product)
        )
    }

    private func discountedPrice(for product: Product) -> Double {
        serviceProductViewModel.calculateDiscountedPrice(
            productId: product.id,
            originalPrice: product.price?.doubleValue ?? 0,
            coupons: userCoupons
        )
    }

    private func bestCouponCode(for product: Product) -> String? {
        let bestCoupon = userCoupons
            .filter { coupon in
                coupon.productIds.isEmpty || coupon.productIds.contains { $0.int32Value == product.id }
            }
            .max { left, right in
                let base = product.price?.doubleValue ?? 0
                let leftDiscount = left.isPercentage ? (base * left.discount / 100) : left.discount
                let rightDiscount = right.isPercentage ? (base * right.discount / 100) : right.discount
                return leftDiscount < rightDiscount
            }

        return bestCoupon?.code
    }
}

struct StripeCheckoutView: View {
    let product: Product
    let couponCode: String?
    var onSuccess: () -> Void

    @Environment(\.dismiss) private var dismiss
    @StateViewModel private var stripeViewModel = SharedDependencies.shared.makeStripeViewModel()
    @StateViewModel private var sessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var errorMessage: String?
    @State private var didStartCheckout = false
    @State private var lastPresentedCommandId: Int64?
    @State private var paymentSheet: PaymentSheet?
    @State private var isPresentingPaymentSheet = false

    var body: some View {
        Group {
            if let paymentSheet {
                checkoutContent
                    .paymentSheet(
                        isPresented: $isPresentingPaymentSheet,
                        paymentSheet: paymentSheet
                    ) { result in
                        handlePaymentResult(result)
                    }
            } else {
                checkoutContent
            }
        }
        .onChange(of: checkoutStateKey) { _ in
            consumeCheckoutState()
        }
        .onChange(of: checkoutPresentationKey) { _ in
            consumeCheckoutPresentation()
        }
    }

    private var checkoutContent: some View {
        VStack(spacing: 16) {
            Text("Pasarela Stripe")
                .font(.title3)
                .fontWeight(.bold)

            if isLoadingState {
                ProgressView("Procesando pago con Stripe...")
            } else if let errorMessage {
                Text(errorMessage).foregroundColor(.red).multilineTextAlignment(.center)
                Button("Reintentar") {
                    lastPresentedCommandId = nil
                    startCheckout()
                }
            } else {
                Text("Vas a completar el pago del producto con Stripe.")
                    .multilineTextAlignment(.center)

                Button("Confirmar pago") {
                    startCheckout()
                }
                .buttonStyle(.borderedProminent)
            }
        }
        .padding(24)
        .onAppear {
            startCheckout()
        }
    }

    private func handlePaymentResult(_ result: PaymentSheetResult) {
        switch result {
        case .completed:
            stripeViewModel.onCheckoutCompleted()
        case .canceled:
            stripeViewModel.onCheckoutCanceled()
        case .failed(let error):
            stripeViewModel.onCheckoutFailed(message: error.localizedDescription)
        }

        paymentSheet = nil
        isPresentingPaymentSheet = false
    }

    private var checkoutStateKey: String {
        let kind = stripeViewModel.checkoutStateKind()
        switch kind {
        case "failed":
            return "failed:\(stripeViewModel.checkoutStateMessage() ?? "")"
        case "ready":
            return "ready:\(stripeViewModel.checkoutReadyClientSecret() ?? "")"
        default:
            return kind
        }
    }

    private var checkoutPresentationKey: String {
        guard let presentationId = stripeViewModel.checkoutPresentationId(),
              let clientSecret = stripeViewModel.checkoutPresentationClientSecret()
        else {
            return "none"
        }
        return "\(presentationId)|\(clientSecret)"
    }

    private var isLoadingState: Bool {
        let kind = stripeViewModel.checkoutStateKind()
        return kind == "loading" || kind == "processing"
    }

    private func startCheckout() {
        if didStartCheckout && errorMessage == nil {
            return
        }

        guard let userId = sessionViewModel.userData?.id else {
            errorMessage = "Debes iniciar sesión para pagar"
            return
        }

        didStartCheckout = true
        errorMessage = nil
        stripeViewModel.resetStartCheckoutState()

        let normalizedType = (product.typeOfProduct ?? "").lowercased()

        if normalizedType == "recurrent" {
            guard let priceId = product.priceId, !priceId.isEmpty else {
                errorMessage = "No se ha podido iniciar la suscripción."
                didStartCheckout = false
                return
            }

            stripeViewModel.startSubscriptionCheckout(
                priceId: priceId,
                userId: userId,
                productId: Int32(Int(product.id)),
                couponCode: couponCode
            )
            return
        }

        stripeViewModel.startSingleProductCheckout(
            amount: product.price?.doubleValue ?? 0,
            currency: "eur",
            productId: Int32(Int(product.id)),
            userId: userId,
            couponCode: couponCode,
            billing: nil
        )
    }

    private func consumeCheckoutState() {
        let stateKind = stripeViewModel.checkoutStateKind()

        if stateKind == "completed" {
            stripeViewModel.resetStartCheckoutState()
            onSuccess()
            dismiss()
            return
        }

        if stateKind == "canceled" {
            errorMessage = "Pago cancelado."
            stripeViewModel.resetStartCheckoutState()
            paymentSheet = nil
            return
        }

        if stateKind == "failed" {
            errorMessage = stripeViewModel.checkoutStateMessage()
            paymentSheet = nil
            return
        }
    }

    private func consumeCheckoutPresentation() {
        guard let presentationId = stripeViewModel.checkoutPresentationId(),
              let clientSecret = stripeViewModel.checkoutPresentationClientSecret(),
              let config = stripeViewModel.checkoutPresentationConfig()
        else {
            return
        }

        if lastPresentedCommandId == presentationId {
            return
        }

        lastPresentedCommandId = presentationId
        presentPaymentSheet(
            clientSecret: clientSecret,
            config: config
        )
    }

    private func presentPaymentSheet(clientSecret: String, config: StripeCheckoutConfig) {
        STPAPIClient.shared.publishableKey = config.publishableKey

        var paymentConfig = PaymentSheet.Configuration()
        paymentConfig.returnURL = "humanperform://stripe-redirect"
        paymentConfig.merchantDisplayName = config.merchantDisplayName
        paymentConfig.allowsDelayedPaymentMethods = config.allowsDelayedPaymentMethods
        if let customerId = config.customerId,
           let ephemeral = config.customerEphemeralKeySecret {
            paymentConfig.customer = .init(id: customerId, ephemeralKeySecret: ephemeral)
        }

        paymentConfig.defaultBillingDetails.name = config.billingName
        paymentConfig.defaultBillingDetails.email = config.billingEmail

        paymentSheet = PaymentSheet(
            paymentIntentClientSecret: clientSecret,
            configuration: paymentConfig
        )
        stripeViewModel.onSheetPresented()
        isPresentingPaymentSheet = true
    }
}

struct PaymentSuccessView: View {
    var onContinueShopping: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 64))
                .foregroundColor(.green)
            Text("Pago completado")
                .font(.title2)
                .fontWeight(.semibold)
            Button("Seguir comprando", action: onContinueShopping)
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding(24)
    }
}
