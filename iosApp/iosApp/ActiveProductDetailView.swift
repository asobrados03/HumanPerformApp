import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct ActiveProductDetailView: View {
    let productId: Int

    @StateViewModel private var serviceProductVM = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()

    private func productImageURL(from imageName: String?) -> URL? {
        guard let imageName,
              let encoded = imageName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
        else {
            return nil
        }

        return URL(string: "\(HttpClientProviderKt.API_BASE_URL)/product_images/\(encoded)")
    }

    var body: some View {
        Group {
            switch serviceProductVM.activeProductDetailStateKind() {
            case "loading":
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

            case "error":
                VStack(spacing: 10) {
                    Text(serviceProductVM.activeProductDetailStateMessage() ?? "Error al cargar detalle")
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                    Button("Reintentar") { fetchDetail() }
                }
                .padding(16)
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            default:
                if let product = serviceProductVM.activeProductDetailStateProduct() {
                    detailContent(product: product)
                } else {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
        }
        .navigationTitle("Detalle")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { fetchDetail() }
        .onChange(of: sessionVM.userData?.id) { _ in fetchDetail() }
    }

    private func fetchDetail() {
        guard let userId = sessionVM.userData?.id else { return }
        serviceProductVM.fetchActiveProductDetail(userId: userId, productId: Int32(productId))
    }

    private func normalizeDate(_ value: String?) -> String? {
        guard let value, !value.isEmpty else { return nil }
        return String(value.prefix(10))
    }

    @ViewBuilder
    private func detailContent(product: ProductDetailResponse) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let imageURL = productImageURL(from: product.image) {
                    AsyncImage(url: imageURL) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        Color.gray.opacity(0.2)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 240)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Text(product.name)
                    .font(.title3)
                    .fontWeight(.bold)

                Text(product.description_ ?? "No hay descripción disponible.")
                    .foregroundColor(.secondary)

                if let createdAt = normalizeDate(product.createdAt) {
                    Text("Fecha de obtención: \(createdAt)")
                }
                if let expiryDate = normalizeDate(product.expiryDate) {
                    Text("Fecha de caducidad: \(expiryDate)")
                }
                if let amount = product.amount?.doubleValue {
                    Text("Precio: \(String(format: "%.2f", amount))€")
                }
                if let discount = product.discount?.doubleValue {
                    Text("Descuento: \(String(format: "%.2f", discount))€")
                }
                if let totalAmount = product.totalAmount?.doubleValue {
                    Text("Total pagado: \(String(format: "%.2f", totalAmount))€")
                }
                if let paymentMethod = product.paymentMethod {
                    Text("Pago con: \(paymentMethod.capitalized)")
                }
                if let paymentStatus = product.paymentStatus {
                    Text("Estado de pago: \(paymentStatus.capitalized)")
                }

                Text("Servicios incluidos:")
                    .font(.headline)
                    .padding(.top, 8)

                if product.services.isEmpty {
                    Text("No hay servicios asociados.")
                } else {
                    ForEach(product.services, id: \.id) { service in
                        Text("• \(service.name)")
                    }
                }
            }
            .padding(16)
        }
    }
}
