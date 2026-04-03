import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireServicesView: View {
    @StateViewModel var viewModel: shared.ServiceProductViewModel = SharedDependencies.shared.makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserSessionViewModel = SharedDependencies.shared.makeUserSessionViewModel()

    var onOpenHireProducts: (Int) -> Void = { _ in }

    private var serviceStateKind: String {
        viewModel.serviceStateKind()
    }

    private var services: [ServiceUiModel] {
        viewModel.serviceStateServices()
    }

    private var serviceErrorMessage: String? {
        viewModel.serviceStateMessage()
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                if serviceStateKind == "loading" {
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                } else if serviceStateKind == "success" {
                    ForEach(services, id: \.service.id) { model in
                        ServiceRow(model: model)
                            .onTapGesture {
                                onOpenHireProducts(Int(model.service.id))
                            }
                    }
                } else if serviceStateKind == "error" {
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
