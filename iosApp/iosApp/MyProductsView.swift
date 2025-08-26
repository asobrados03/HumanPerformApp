//
//  MyProductsView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

struct MyProductsView: View {
    @StateObject var serviceProductViewModel: ServiceProductViewModel = ServiceProductViewModel()
    @StateObject var userViewModel: UserViewModel = UserViewModel()
    @State private var selectedProduct: ServiceItem? = nil
    @State private var showProductOptions = false
    @State private var showUnsubscribeConfirm = false

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 8) {
                if serviceProductViewModel.userProducts.isEmpty {
                    Text("No tienes productos contratados.")
                        .foregroundColor(.secondary)
                } else {
                    ForEach(serviceProductViewModel.userProductsDistinct, id: \.id) { producto in
                        ProductRow(producto: producto)
                            .onTapGesture {
                                selectedProduct = producto
                                showProductOptions = true
                            }
                    }
                }
            }
            .padding(12)
        }
        .onAppear {
            if let id = userViewModel.currentUserId {
                serviceProductViewModel.loadUserProducts(userId: id)
            }
        }
        .onChange(of: userViewModel.currentUserId) { newId in
            if let id = newId {
                serviceProductViewModel.loadUserProducts(userId: id)
            }
        }
        .confirmationDialog(
            selectedProduct.map { "Producto: \($0.name)" } ?? "",
            isPresented: $showProductOptions, titleVisibility: .visible
        ) {
            Button("Ver detalles") {
                // Navegar a detalle (podría usar NavigationLink, o setear estado para NavigationDestination)
                serviceProductViewModel.productoSeleccionado = selectedProduct
                // Suponiendo que ServicesView está en NavigationView, podríamos navegar:
                // navigateToProductDetail(selectedProduct)
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
                if let prod = selectedProduct {
                    serviceProductViewModel.unassignProductFromUser(productId: prod.id, userId: userViewModel.currentUserId ?? -1)
                }
                selectedProduct = nil
            }
        } message: {
            Text("¿Estás seguro de que quieres darte de baja de este producto?")
        }
    }
}

