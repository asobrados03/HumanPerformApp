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
        vm.coachesState is CoachStateLoading || vm.getPreferredCoachState is GetPreferredCoachStateLoading || vm.markFavoriteState is MarkFavoriteStateLoading
    }

    private var coaches: [Professional] {
        (vm.coachesState as? CoachStateSuccess)?.coaches ?? []
    }

    var body: some View {
        Group {
            if isLoading && coaches.isEmpty {
                ProgressView()
            } else {
                List {
                    Section {
                        ForEach(coaches, id: \.id) { coach in
                            let isSelected = coach.id == preferredCoachId
                            HStack(spacing: 12) {
                                coachImage(for: coach, selected: isSelected)
                                Text(coach.name)
                                    .font(.body)
                                    .foregroundStyle(isSelected ? Color.white : .primary)
                                    .lineLimit(1)
                                    .truncationMode(.tail)
                            }
                            .padding(.vertical, 8)
                            .contentShape(Rectangle())
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 12))
                            .listRowBackground(isSelected ? selectedColor : Color(.systemBackground))
                            .onTapGesture {
                                vm.markFavorite(coachId: coach.id, serviceName: nil, userId: sessionVM.userData?.id)
                            }
                        }
                    } header: {
                        Text("Profesionales del deporte")
                            .font(.headline).textCase(nil)
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
        .task {
            vm.getCoaches()
            if let id = sessionVM.userData?.id { vm.getPreferredCoach(userId: id) }
        }
        .onChange(of: vm.markFavoriteState) { state in
            switch state {
            case let success as MarkFavoriteStateSuccess:
                alertMessage = success.message
                preferredCoachId = nil
                if let id = sessionVM.userData?.id { vm.getPreferredCoach(userId: id) }
                vm.clearMarkFavoriteState()
            case let error as MarkFavoriteStateError:
                alertMessage = error.message
                vm.clearMarkFavoriteState()
            default:
                break
            }
        }
        .onChange(of: vm.getPreferredCoachState) { state in
            switch state {
            case let success as GetPreferredCoachStateSuccess:
                preferredCoachId = Int(success.coachId)
            case is GetPreferredCoachStateError:
                preferredCoachId = nil
            default:
                break
            }
        }
        .alert(alertMessage ?? "", isPresented: Binding(
            get: { alertMessage != nil },
            set: { if !$0 { alertMessage = nil } }
        )) {
            Button("OK", role: .cancel) { alertMessage = nil }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }

    private func coachImage(for coach: Professional, selected: Bool) -> some View {
        let base = "\(HttpClientProviderKt.API_BASE_URL)/profile_pic/"
        let circleStroke = selected ? Color.white.opacity(0.7) : Color.secondary.opacity(0.25)

        if let photo = coach.photoName,
           let encoded = photo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
           let url = URL(string: base + encoded) {
            return AnyView(
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView().frame(width: avatarSize, height: avatarSize)
                    case .success(let img):
                        img.resizable()
                            .scaledToFill()
                            .frame(width: avatarSize, height: avatarSize)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(circleStroke, lineWidth: 1))
                    case .failure:
                        placeholderIcon(selected: selected)
                    @unknown default:
                        placeholderIcon(selected: selected)
                    }
                }
            )
        } else {
            return AnyView(placeholderIcon(selected: selected))
        }
    }

    @ViewBuilder
    private func placeholderIcon(selected: Bool) -> some View {
        ZStack {
            Circle()
                .fill(selected ? Color.white.opacity(0.18) : Color(.systemGray5))
            Image(systemName: "person.fill")
                .imageScale(.medium)
                .foregroundColor(selected ? .white : .secondary)
        }
        .frame(width: avatarSize, height: avatarSize)
        .overlay(Circle().stroke(selected ? Color.white.opacity(0.7) : Color.secondary.opacity(0.25), lineWidth: 1))
    }
}
