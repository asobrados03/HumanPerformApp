import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireServicesView: View {
    @StateViewModel var viewModel: shared.ServiceProductViewModel = makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserViewModel = makeUserViewModel()

    var onOpenHireProducts: (Int) -> Void = { _ in }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                switch viewModel.serviceUiState {
                case is ServiceUiStateLoading:
                    ProgressView()
                        .frame(maxWidth: .infinity, alignment: .center)
                case let success as ServiceUiStateSuccess:
                    ForEach(success.services, id: \.service.id) { model in
                        ServiceRow(model: model)
                            .onTapGesture {
                                onOpenHireProducts(model.service.id)
                            }
                    }
                case let error as ServiceUiStateError:
                    VStack(spacing: 8) {
                        Text(error.message)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)

                        if let userId = sessionViewModel.currentUser?.id {
                            Button("Reintentar") {
                                viewModel.loadAllServices(userId: userId)
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
            if let userId = sessionViewModel.currentUser?.id {
                viewModel.loadAllServices(userId: userId)
            }
        }
        .onChange(of: sessionViewModel.currentUser?.id) { newUserId in
            if let userId = newUserId {
                viewModel.loadAllServices(userId: userId)
            }
        }
    }
}
