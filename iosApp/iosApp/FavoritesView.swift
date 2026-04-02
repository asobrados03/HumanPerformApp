//
//  FavoritesView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Pantalla que muestra la lista de entrenadores y permite marcar un favorito.
struct FavoritesView: View {

    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var vm = SharedDependencies.shared.makeUserFavoritesViewModel()

    @State private var preferredCoachId: Int?
    @State private var alertMessage: String?

    /// Color de fondo para el entrenador seleccionado (0xAAF683).
    private let selectedColor = Color(red: 170/255, green: 246/255, blue: 131/255)

    private let avatarSize: CGFloat = 36

    private var isLoading: Bool {
        vm.coachesStateKind() == "loading"
        || vm.preferredCoachStateKind() == "loading"
        || vm.markFavoriteStateKind() == "loading"
    }

    private var coaches: [Professional] {
        vm.coachesList()
    }

    private var isAlertPresented: Binding<Bool> {
        Binding(
            get: { alertMessage != nil },
            set: { newValue in
                if !newValue {
                    alertMessage = nil
                }
            }
        )
    }

    private var alertTitle: String {
        alertMessage ?? ""
    }

    var body: some View {
        Group {
            if isLoading && coaches.isEmpty {
                ProgressView()
            } else {
                List {
                    Section {
                        ForEach(coaches, id: \.id) { coach in
                            CoachRow(
                                coach: coach,
                                isSelected: coach.id == preferredCoachId,
                                avatarSize: avatarSize,
                                selectedColor: selectedColor
                            ) {
                                vm.markFavorite(
                                    coachId: coach.id,
                                    serviceName: nil,
                                    userId: sessionVM.userData?.id
                                )
                            }
                        }
                    } header: {
                        Text("Profesionales del deporte")
                            .font(.headline)
                            .textCase(nil)
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
        .task {
            vm.getCoaches()
            if let id = sessionVM.userData?.id {
                vm.getPreferredCoach(userId: id)
            }
        }
        .onChange(of: vm.markFavoriteState) { _ in
            switch vm.markFavoriteStateKind() {
            case "success":
                alertMessage = vm.markFavoriteStateMessage()
                preferredCoachId = nil
                if let id = sessionVM.userData?.id {
                    vm.getPreferredCoach(userId: id)
                }
                vm.clearMarkFavoriteState()

            case "error":
                alertMessage = vm.markFavoriteStateMessage()
                vm.clearMarkFavoriteState()

            default:
                break
            }
        }
        .onChange(of: vm.getPreferredCoachState) { _ in
            switch vm.preferredCoachStateKind() {
            case "success":
                preferredCoachId = vm.preferredCoachId().map(Int.init)

            case "error":
                preferredCoachId = nil

            default:
                break
            }
        }
        .alert(alertTitle, isPresented: isAlertPresented) {
            Button("OK", role: .cancel) {
                alertMessage = nil
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo()
            }
        }
    }
}
