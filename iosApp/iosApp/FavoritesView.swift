//
//  FavoritesView.swift
//  iosApp
//
//  Created by ChatGPT on 2025-08-27.
//

import SwiftUI
import shared

/// Pantalla que muestra la lista de entrenadores y permite marcar un favorito.
struct FavoritesView: View {
    @EnvironmentObject var userVM: UserViewModel
    @StateObject private var vm = FavoritesViewModel()

    /// Color de fondo para el entrenador seleccionado (0xAAF683).
    private let selectedColor = Color(red: 170/255, green: 246/255, blue: 131/255)

    var body: some View {
        Group {
            if vm.isLoading {
                ProgressView()
            } else {
                List {
                    Section(header: Text("Profesionales del deporte").fontWeight(.bold)) {
                        ForEach(vm.coaches, id: \.id) { coach in
                            let isSelected = coach.id == vm.preferredCoachId
                            HStack(alignment: .center, spacing: 12) {
                                coachImage(for: coach, selected: isSelected)
                                Text(coach.name)
                                    .foregroundColor(isSelected ? .white : .primary)
                            }
                            .padding(.vertical, 8)
                            .listRowInsets(EdgeInsets())
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .listRowBackground(isSelected ? selectedColor : Color(.systemBackground))
                            .onTapGesture {
                                vm.markFavorite(coach: coach, userId: userVM.currentUser?.id)
                            }
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
        .onAppear {
            vm.loadCoaches()
            if let id = userVM.currentUser?.id {
                vm.loadPreferredCoach(for: id)
            }
        }
        .alert(vm.alertMessage ?? "", isPresented: Binding(
            get: { vm.alertMessage != nil },
            set: { if !$0 { vm.alertMessage = nil } }
        )) {
            Button("OK", role: .cancel) { vm.alertMessage = nil }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }

    /// Imagen del entrenador o icono por defecto.
    private func coachImage(for coach: Professional, selected: Bool) -> some View {
        let base = "\(ApiClient.shared.baseUrl)/profile_pic/"
        if let photo = coach.photoName,
           let encoded = photo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
           let url = URL(string: base + encoded) {
            return AnyView(AsyncImage(url: url) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                case .success(let img):
                    img.resizable().scaledToFill()
                case .failure:
                    Image(systemName: "person.circle.fill").resizable()
                        .foregroundColor(selected ? .white : .secondary)
                @unknown default:
                    Image(systemName: "person.circle.fill").resizable()
                        .foregroundColor(selected ? .white : .secondary)
                }
            }
            .frame(width: 40, height: 40)
            .clipShape(Circle()))
        } else {
            return AnyView(Image(systemName: "person.circle.fill")
                .resizable()
                .frame(width: 40, height: 40)
                .foregroundColor(selected ? .white : .secondary))
        }
    }
}

