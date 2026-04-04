import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireProductsView: View {
    let serviceId: Int
    var onOpenProductDetail: (Int) -> Void = { _ in }

    @StateViewModel private var serviceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel private var sessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()

    @State private var selectedType: String = "all"
    @State private var selectedSessions: Int32 = 0

    private var productsStateKind: String {
        serviceProductViewModel.serviceProductsStateKind(serviceId: Int32(serviceId))
    }

    private var productsErrorMessage: String? {
        serviceProductViewModel.serviceProductsStateMessage(serviceId: Int32(serviceId))
    }

    private var hiredIds: Set<Int32> {
        Set(serviceProductViewModel.userProductsStateProducts().map { $0.id })
    }

    private var availableProducts: [Product] {
        let base = serviceProductViewModel.serviceProductsStateServices(serviceId: Int32(serviceId))

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

            let sessionOk = selectedSessions == 0 || product.session?.int32Value == selectedSessions
            return typeOk && sessionOk
        }
    }

    private var availableSessions: [Int32] {
        let services = serviceProductViewModel.serviceProductsStateServices(serviceId: Int32(serviceId))
        return Array(Set(services.compactMap { $0.session?.int32Value })).sorted()
    }

    var body: some View {
        VStack(spacing: 10) {
            filtersView

            if productsStateKind == "loading" {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if productsStateKind == "error" {
                VStack(spacing: 8) {
                    Text(productsErrorMessage ?? "Error desconocido")
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)

                    Button("Reintentar") {
                        guard let userId = sessionViewModel.userData?.id else { return }
                        serviceProductViewModel.loadServiceProducts(serviceId: Int32(serviceId), userId: userId)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if productsStateKind == "success" {
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
                                    onOpenProductDetail(Int(product.id))
                                }
                        }
                    }
                    .padding(12)
                }
            } else {
                EmptyView()
            }
        }
        .navigationTitle("Contratar")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadDataIfPossible()
        }
        .onChange(of: sessionViewModel.userData?.id) { _ in
            loadDataIfPossible()
        }
    }

    private func loadDataIfPossible() {
        guard let userId = sessionViewModel.userData?.id else { return }
        serviceProductViewModel.loadServiceProducts(serviceId: Int32(serviceId), userId: userId)
        serviceProductViewModel.loadUserProducts(userId: userId)
        serviceProductViewModel.loadUserCoupons(userId: userId)
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
                Text("Todas").tag(Int32(0))
                ForEach(availableSessions, id: \.self) { s in
                    Text("\(s)").tag(s)
                }
            }
            .pickerStyle(.menu)
        }
        .padding(.horizontal, 12)
    }
}
