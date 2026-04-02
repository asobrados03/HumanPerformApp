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

    private var coachRows: [CoachUI] {
        vm.coachesList().toCoachUIs()
    }

    private var isLoading: Bool {
        vm.coachesStateKind() == "loading"
            || vm.preferredCoachStateKind() == "loading"
            || vm.markFavoriteStateKind() == "loading"
    }

    private var isAlertPresented: Binding<Bool> {
        Binding(
            get: { alertMessage != nil },
            set: { shouldPresent in
                if !shouldPresent {
                    alertMessage = nil
                }
            }
        )
    }

    var body: some View {
        content
            .task(perform: loadInitialData)
            .onChange(of: vm.markFavoriteState) { _ in
                handleMarkFavoriteStateChange()
            }
            .onChange(of: vm.getPreferredCoachState) { _ in
                handlePreferredCoachStateChange()
            }
            .alert(alertMessage ?? "", isPresented: isAlertPresented) {
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

    @ViewBuilder
    private var content: some View {
        if isLoading && coachRows.isEmpty {
            ProgressView()
        } else {
            coachList
        }
    }

    private var coachList: some View {
        List {
            Section {
                ForEach(coachRows) { coach in
                    CoachRow(
                        coach: coach,
                        isSelected: coach.id == preferredCoachId,
                        avatarSize: avatarSize,
                        selectedColor: selectedColor
                    ) {
                        vm.markFavorite(
                            coachId: Int32(coach.id),
                            serviceName: coach.serviceName,
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

    private func loadInitialData() {
        vm.getCoaches()
        if let userId = sessionVM.userData?.id {
            vm.getPreferredCoach(userId: userId)
        }
    }

    private func handleMarkFavoriteStateChange() {
        switch vm.markFavoriteStateKind() {
        case "success":
            alertMessage = vm.markFavoriteStateMessage()
            preferredCoachId = nil
            if let userId = sessionVM.userData?.id {
                vm.getPreferredCoach(userId: userId)
            }
            vm.clearMarkFavoriteState()

        case "error":
            alertMessage = vm.markFavoriteStateMessage()
            vm.clearMarkFavoriteState()

        default:
            break
        }
    }

    private func handlePreferredCoachStateChange() {
        switch vm.preferredCoachStateKind() {
        case "success":
            preferredCoachId = vm.preferredCoachId().map(Int.init)
        case "error":
            preferredCoachId = nil
        default:
            break
        }
    }
}
