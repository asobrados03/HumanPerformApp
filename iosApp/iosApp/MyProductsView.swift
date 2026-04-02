import SwiftUI
import shared

struct MyProductsView: View {
    @State var serviceProductViewModel: shared.ServiceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @State var sessionViewModel: shared.UserSessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()
    @State private var selectedProduct: Product? = nil
    @State private var showProductOptions = false
    @State private var showUnsubscribeConfirm = false

    var onOpenProductDetail: (Int) -> Void = { _ in }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                switch serviceProductViewModel.userProductsState {
                case is UserProductsUiStateLoading:
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                case let success as UserProductsUiStateSuccess:
                    if success.products.isEmpty {
                        Text("No tienes productos contratados.")
                            .foregroundColor(.secondary)
                    } else {
                        ForEach(success.products, id: \.id) { producto in
                            ProductRow(producto: producto)
                                .onTapGesture {
                                    selectedProduct = producto
                                    showProductOptions = true
                                }
                        }
                    }
                case let error as UserProductsUiStateError:
                    VStack(spacing: 8) {
                        Text(error.message)
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
            selectedProduct.map { "Producto: \($0.name)" } ?? "",
            isPresented: $showProductOptions,
            titleVisibility: .visible
        ) {
            Button("Ver detalles") {
                if let productId = selectedProduct?.id {
                    onOpenProductDetail(productId)
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
