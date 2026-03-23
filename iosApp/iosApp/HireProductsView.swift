import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireProductsView: View {
    let serviceId: Int
    var onOpenProductDetail: (Int) -> Void = { _ in }

    @StateViewModel private var serviceProductViewModel = makeServiceProductViewModel()
    @StateViewModel private var sessionViewModel = makeUserSessionViewModel()

    @State private var selectedType: String = "all"
    @State private var selectedSessions: Int = 0

    private var productsState: ServiceProductUiState {
        if let state = serviceProductViewModel.serviceProducts[serviceId] {
            return state
        }
        return ServiceProductUiStateLoading()
    }

    private var hiredIds: Set<Int> {
        guard let state = serviceProductViewModel.userProductsState as? UserProductsUiStateSuccess else {
            return []
        }
        return Set(state.products.map { $0.id })
    }

    private var availableProducts: [Product] {
        guard let state = productsState as? ServiceProductUiStateSuccess else { return [] }
        let base = state.services

        return base.filter { product in
            let typeOk: Bool
            switch selectedType {
            case "recurrent":
                typeOk = product.typeOfProduct == "recurrent"
            case "non_recurrent":
                typeOk = product.typeOfProduct != "recurrent"
            default:
                typeOk = true
            }

            let sessionOk = selectedSessions == 0 || product.session?.intValue == selectedSessions
            return typeOk && sessionOk
        }
    }

    private var availableSessions: [Int] {
        guard let state = productsState as? ServiceProductUiStateSuccess else { return [] }
        return Array(Set(state.services.compactMap { $0.session?.intValue })).sorted()
    }

    var body: some View {
        VStack(spacing: 10) {
            filtersView

            switch productsState {
            case is ServiceProductUiStateLoading:
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            case let error as ServiceProductUiStateError:
                VStack(spacing: 8) {
                    Text(error.message).foregroundColor(.red).multilineTextAlignment(.center)
                    Button("Reintentar") {
                        guard let userId = sessionViewModel.userData?.id else { return }
                        serviceProductViewModel.loadServiceProducts(serviceId: serviceId, userId: userId)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            case is ServiceProductUiStateSuccess:
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(availableProducts, id: \.id) { product in
                            ProductRow(producto: product)
                                .overlay(alignment: .trailing) {
                                    if hiredIds.contains(product.id) {
                                        Text("Adquirido")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                            .padding(.trailing, 8)
                                    }
                                }
                                .onTapGesture {
                                    onOpenProductDetail(product.id)
                                }
                        }
                    }
                    .padding(12)
                }
            default:
                EmptyView()
            }
        }
        .navigationTitle("Contratar")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            guard let userId = sessionViewModel.userData?.id else { return }
            serviceProductViewModel.loadServiceProducts(serviceId: serviceId, userId: userId)
            serviceProductViewModel.loadUserProducts(userId: userId)
            serviceProductViewModel.loadUserCoupons(userId: userId)
        }
    }

    private var filtersView: some View {
        VStack(spacing: 8) {
            Picker("Tipo", selection: $selectedType) {
                Text("Todos").tag("all")
                Text("Recurrente").tag("recurrent")
                Text("No recurrente").tag("non_recurrent")
            }
            .pickerStyle(.segmented)

            Picker("Sesiones", selection: $selectedSessions) {
                Text("Todas").tag(0)
                ForEach(availableSessions, id: \.self) { s in
                    Text("\(s)").tag(s)
                }
            }
            .pickerStyle(.menu)
        }
        .padding(.horizontal, 12)
    }
}
