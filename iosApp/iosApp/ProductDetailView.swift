import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

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

    @StateViewModel private var serviceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel private var sessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var isProcessing = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Pasarela Stripe")
                .font(.title3)
                .fontWeight(.bold)

            if isProcessing {
                ProgressView("Procesando pago con Stripe...")
            } else if let errorMessage {
                Text(errorMessage).foregroundColor(.red).multilineTextAlignment(.center)
                Button("Reintentar") {
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
    }

    private func startCheckout() {
        guard let userId = sessionViewModel.userData?.id else {
            errorMessage = "Debes iniciar sesión para pagar"
            return
        }

        isProcessing = true
        serviceProductViewModel.assignProductToUser(
            userId: userId,
            productId: product.id,
            paymentMethod: "stripe",
            couponCode: couponCode
        )

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            isProcessing = false
            onSuccess()
        }
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
