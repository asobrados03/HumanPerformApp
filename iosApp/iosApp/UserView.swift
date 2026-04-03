//
//  UserView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

private enum UserMenuOption: String, CaseIterable, Identifiable {
    case configuracion    = "Configuración"
    case favoritos        = "Mis favoritos"
    case documento        = "Documento"
    case verPago          = "Ver métodos de pago"
    case monederoVirtual  = "Monedero virtual"
    case anadirCupon      = "Añadir cupón"

    var id: String { rawValue }
}

struct UserView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var walletVM = SharedDependencies.shared.makeUserWalletViewModel()
    @StateViewModel private var profileVM = SharedDependencies.shared.makeUserProfileViewModel()

    private var balanceValue: Double {
        walletVM.balance?.doubleValue ?? 0
    }

    var body: some View {
        Group {
            if UITestConfig.isMockNetworkEnabled {
                ScrollView {
                    VStack(spacing: 16) {
                        Text("Mock User Loaded")
                            .font(.headline)
                            .accessibilityIdentifier("userLoadedMarker")

                        NavigationLink {
                            MyProfileView()
                        } label: {
                            Text("Mi perfil")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.white)
                                .cornerRadius(10)
                        }
                        .accessibilityIdentifier("myProfileButton")
                    }
                    .padding()
                }
            } else {
            if sessionVM.isLoading {
                ProgressView()
            } else if let user = sessionVM.userData {
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
                .onChange(of: sessionVM.userData?.id) { id in
                    if let id = id { walletVM.loadBalance(userId: id) }
                }
            } else {
                Text("Sin usuario")
            }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .accessibilityIdentifier("userView")
        .onAppear {
            guard !UITestConfig.isMockNetworkEnabled else { return }
            refreshProfileAndBalance()
        }
    }

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

            Text("Saldo: \(balanceValue, specifier: "%.2f") €")
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
                    MyProfileView()
                }

                actionButton(title: "Editar perfil", isPrimary: false) {
                    EditProfileView()
                }
            }
            .padding(.horizontal, 16)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(Color.red)
    }

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

    private func refreshProfileAndBalance() {
        if let user = sessionVM.userData {
            profileVM.fetchUserProfile(currentUser: sessionVM.currentUserState())
            walletVM.loadBalance(userId: user.id)
        }
    }

    @ViewBuilder
    private func destinationView(for option: UserMenuOption) -> some View {
        switch option {
        case .configuracion:
            ConfigurationView()
        case .favoritos:
            FavoritesView()
        case .documento:
            DocumentView()
        case .verPago:
            PaymentMethodsView()
        case .monederoVirtual:
            ElectronicWalletView()
        case .anadirCupon:
            AddCouponView()
        }
    }
}
