//
//  FavoritesView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct FavoritesView: View {

    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var vm = SharedDependencies.shared.makeUserFavoritesViewModel()

    @State private var preferredCoachId: Int?
    @State private var alertMessage: String?

    private let selectedColor = Color(red: 170/255, green: 246/255, blue: 131/255)
    private let avatarSize: CGFloat = 36

    private var currentUserId: KotlinInt? {
        sessionVM.userData.map { KotlinInt(value: $0.id) }
    }

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
                if !shouldPresent { alertMessage = nil }
            }
        )
    }

    var body: some View {
        content
            .task {
                await loadInitialData()
            }
            .onChange(of: vm.markFavoriteState) { _ in
                handleMarkFavoriteStateChange()
            }
            .onChange(of: vm.getPreferredCoachState) { _ in
                handlePreferredCoachStateChange()
            }
            .onChange(of: sessionVM.userData?.id) { _ in
                Task { await loadPreferredCoach() }
            }
            .alert(alertMessage ?? "", isPresented: isAlertPresented) {
                Button("OK", role: .cancel) { alertMessage = nil }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) { NavBarLogo() }
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
                            userId: currentUserId
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

    private func loadInitialData() async {
        vm.getCoaches()
        await loadPreferredCoach()
    }

    private func loadPreferredCoach() async {
        if let userId = currentUserId {
            vm.getPreferredCoach(userId: userId)
        }
    }

    private func handleMarkFavoriteStateChange() {
        switch vm.markFavoriteStateKind() {
        case "success":
            alertMessage = vm.markFavoriteStateMessage()
            preferredCoachId = nil
            if let userId = currentUserId {
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
            preferredCoachId = vm.preferredCoachId().map { Int($0.int32Value) }
        case "error":
            preferredCoachId = nil
        default:
            break
        }
    }
}
