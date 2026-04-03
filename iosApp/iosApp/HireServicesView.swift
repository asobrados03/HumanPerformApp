import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireServicesView: View {
    @StateViewModel var viewModel: shared.ServiceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserSessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()

    var onOpenHireProducts: (Int) -> Void = { _ in }

    private var serviceStateName: String {
        String(describing: type(of: viewModel.serviceUiState))
    }

    private var services: [ServiceUiModel] {
        Mirror(reflecting: viewModel.serviceUiState)
            .children
            .first(where: { $0.label == "services" })?
            .value as? [ServiceUiModel] ?? []
    }

    private var serviceErrorMessage: String? {
        Mirror(reflecting: viewModel.serviceUiState)
            .children
            .first(where: { $0.label == "message" })?
            .value as? String
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                if serviceStateName.contains("Loading") {
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                } else if serviceStateName.contains("Success") {
                    ForEach(services, id: \.service.id) { model in
                        ServiceRow(model: model)
                            .onTapGesture {
                                onOpenHireProducts(Int(model.service.id))
                            }
                    }
                } else if serviceStateName.contains("Error") {
                    VStack(spacing: 8) {
                        Text(serviceErrorMessage ?? "Error desconocido")
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)

                        if let userId = sessionViewModel.userData?.id {
                            Button("Reintentar") {
                                viewModel.loadAllServices(userId: userId)
                            }
                        }
                    }
                } else {
                    EmptyView()
                }
            }
            .padding(12)
        }
        .onAppear {
            if let userId = sessionViewModel.userData?.id {
                viewModel.loadAllServices(userId: userId)
            }
        }
        .onChange(of: sessionViewModel.userData?.id) { newUserId in
            if let userId = newUserId {
                viewModel.loadAllServices(userId: userId)
            }
        }
    }
}
