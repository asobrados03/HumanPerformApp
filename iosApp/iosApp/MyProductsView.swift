import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct MyProductsView: View {
    @StateViewModel var serviceProductViewModel: shared.ServiceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserSessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var selectedProduct: Product? = nil
    @State private var showProductOptions = false
    @State private var showUnsubscribeConfirm = false

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
                    serviceProductViewModel.unassignProductFromUser(productId: prod.id, userId: userId)
                }
                selectedProduct = nil
            }
        } message: {
            Text("¿Estás seguro de que quieres darte de baja de este producto?")
        }
    }
}
