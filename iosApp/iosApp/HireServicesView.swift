//
//  HireServicesView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct HireServicesView: View {
    @StateViewModel var viewModel: shared.ServiceProductViewModel = makeServiceProductViewModel()
    @StateViewModel var sessionViewModel: shared.UserViewModel = makeUserViewModel()
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                ForEach(viewModel.allServices, id: \.id) { servicio in
                    ServiceRow(servicio: servicio, contratado: viewModel.userHasService(servicio.id))
                        .onTapGesture {
                            // Navegar a pantalla de contratar producto
                            // e.g., set selected service in view model and navigate
                            viewModel.selectedService = servicio
                            // navigate to HireProductView
                        }
                }
            }
            .padding(12)
        }
        .onAppear {
            // Cuando la vista aparece, cargamos los datos necesarios
            viewModel.loadAllServices()
            if let userId = sessionViewModel.userId {
                viewModel.loadUserProducts(userId: Int32(userId))
            }
        }
    }
}

