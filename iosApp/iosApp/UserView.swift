//
//  UserView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Opciones del menú de usuario mostradas en esta pantalla.
private enum UserMenuOption: String, CaseIterable, Identifiable {
    case configuracion    = "Configuración"
    case favoritos        = "Mis favoritos"
    case documento        = "Documento"
    case verPago          = "Ver métodos de pago"
    case monederoVirtual  = "Monedero virtual"
    case anadirCupon      = "Añadir cupón"

    var id: String { rawValue }
}

/// Vista principal del perfil de usuario en iOS.
/// Muestra la información básica del usuario y un listado de opciones
/// de navegación similares a las presentes en la versión Android.
struct UserView: View {
    @StateViewModel private var vm = makeUserViewModel()

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else if let user = vm.currentUser {
                ScrollView {
                    VStack(spacing: 0) {
                        header(for: user)

                        LazyVStack(spacing: 12) {
                            ForEach(UserMenuOption.allCases) { option in
                                NavigationLink {
                                    destinationView(for: option)
                                } label: {
                                    menuCardRow(title: option.rawValue)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(16)
                    }
                    .padding(.bottom, 16)
                }
                .background(Color(.systemGroupedBackground).ignoresSafeArea())
                .onChange(of: vm.currentUser?.id) { id in
                    if let id = id { vm.loadBalance(for: id) }
                }
            } else {
                Text("Sin usuario")
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onAppear {
            refreshProfileAndBalance()
        }
    }

    /// Encabezado con la información principal del usuario y acciones.
    @ViewBuilder
    private func header(for user: User) -> some View {
        VStack(spacing: 16) {
            HStack {
                Spacer()
                UserProfileImageView(photoName: user.profilePictureName, image: nil)
                Spacer()
            }

            Text(user.fullName)
                .font(.title3)
                .fontWeight(.semibold)
                .foregroundColor(.white)

            Text(user.email)
                .font(.subheadline)
                .foregroundColor(.white)

            Text(user.phone)
                .font(.subheadline)
                .foregroundColor(.white)

            Text("Saldo: \(vm.balance, specifier: "%.2f") €")
                .font(.headline)
                .foregroundColor(.black)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(Color.yellow)
                .cornerRadius(8)
                .padding(.horizontal, 16)

            HStack(spacing: 12) {
                actionButton(title: "Mi perfil", isPrimary: true) {
                    MyProfileView().environmentObject(vm)
                }

                actionButton(title: "Editar perfil", isPrimary: false) {
                    EditProfileView().environmentObject(vm)
                }
            }
            .padding(.horizontal, 16)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(Color.red)
    }

    /// Tarjeta de opción de navegación, con chevron y área táctil completa.
    private func menuCardRow(title: String) -> some View {
        HStack {
            Text(title)
                .font(.body)
                .foregroundColor(.primary)

            Spacer()

            Image(systemName: "chevron.right")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(.secondary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .frame(maxWidth: .infinity)
        .background(Color.white)
        .cornerRadius(12)
        .contentShape(Rectangle())
    }

    @ViewBuilder
    private func actionButton<Destination: View>(title: String, isPrimary: Bool, @ViewBuilder destination: () -> Destination) -> some View {
        NavigationLink {
            destination()
        } label: {
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundColor(isPrimary ? .red : .white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(isPrimary ? Color.white : Color.clear)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.white, lineWidth: 1)
                )
                .cornerRadius(10)
        }
        .buttonStyle(.plain)
    }

    /// Refresca el perfil al entrar en pantalla y, después, actualiza el saldo
    /// para el usuario actual.
    private func refreshProfileAndBalance() {
        vm.fetchUserProfile()
        if let id = vm.currentUser?.id {
            vm.loadBalance(for: id)
        }
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
