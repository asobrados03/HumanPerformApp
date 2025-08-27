//
//  UserView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

/// Opciones del menú de usuario mostradas en esta pantalla.
private enum UserMenuOption: String, CaseIterable, Identifiable {
    case configuracion    = "Configuración"
    case favoritos        = "Mis favoritos"
    case documento        = "Documento"
    case verPago          = "Ver metodos de pago"
    case monederoVirtual  = "Monedero Virtual"
    case anadirCupon      = "Añadir cupón"

    var id: String { rawValue }
}

/// Vista principal del perfil de usuario en iOS.
/// Muestra la información básica del usuario y un listado de opciones
/// de navegación similares a las presentes en la versión Android.
struct UserView: View {
    @StateObject private var vm = UserViewModel()

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else if let user = vm.currentUser {
                List {
                    VStack(spacing: 12) {
                        Image(systemName: "person.circle")
                            .resizable()
                            .frame(width: 80, height: 80)
                            .foregroundColor(.gray)
                        Text(user.fullName)
                            .font(.title3)
                            .fontWeight(.semibold)
                        Text(user.email)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text(user.phone)
                            .font(.subheadline)
                            .foregroundColor(.secondary)

                        Text("Saldo: \(vm.balance, specifier: "%.2f") €")
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(Color.yellow)
                            .cornerRadius(8)
                        HStack(spacing: 12) {
                            NavigationLink("Mi perfil") {
                                MyProfileView().environmentObject(vm)
                            }
                            .buttonStyle(.borderedProminent)

                            NavigationLink("Editar perfil") {
                                EditProfileView().environmentObject(vm)
                            }
                            .buttonStyle(.bordered)
                        }

                    }
                    .frame(maxWidth: .infinity)
                    .listRowInsets(EdgeInsets())
                    .padding(.vertical)
                    .onAppear { vm.loadBalance(for: user.id) }


                    ForEach(UserMenuOption.allCases) { option in
                        NavigationLink(option.rawValue) {
                            destinationView(for: option)
                        }
                    }
                }
                .listStyle(.plain)
            } else {
                Text("Sin usuario")
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }

    // MARK: - Destinos
    @ViewBuilder
    private func destinationView(for option: UserMenuOption) -> some View {
        switch option {
        case .configuracion:
            ConfigurationView().environmentObject(vm)
        case .favoritos:
            FavoritesView().environmentObject(vm)
        case .documento:
            DocumentView().environmentObject(vm)
        case .verPago:
            PaymentMethodsView().environmentObject(vm)
        case .monederoVirtual:
            ElectronicWalletView().environmentObject(vm)
        case .anadirCupon:
            AddCouponView().environmentObject(vm)
        }
    }
}

