import SwiftUI
import shared

/// Pantalla de configuración del usuario.
struct ConfigurationView: View {
    @EnvironmentObject var userVM: UserViewModel
    @EnvironmentObject var appState: AppState

    @State private var showLogoutAlert = false
    @State private var showDeleteAlert = false
    @State private var isProcessing = false
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            List {
                Section {
                    if let user = userVM.currentUser {
                        Text(user.fullName)
                            .font(.headline)
                    }
                    Button("Cerrar sesión") { showLogoutAlert = true }
                    Button("Eliminar cuenta") { showDeleteAlert = true }
                        .foregroundColor(.red)
                    NavigationLink("Cambiar contraseña") {
                        ChangePasswordView().environmentObject(userVM)
                    }
                }
            }
            .listStyle(.insetGrouped)

            if isProcessing {
                Color.black.opacity(0.4).ignoresSafeArea()
                ProgressView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .alert("Cerrar sesión", isPresented: $showLogoutAlert) {
            Button("Cancelar", role: .cancel) {}
            Button("Sí", role: .destructive) { logout() }
        } message: {
            Text("¿Estás seguro de que quieres cerrar la sesión?")
        }
        .alert("Eliminar cuenta", isPresented: $showDeleteAlert) {
            Button("Cancelar", role: .cancel) {}
            Button("Eliminar", role: .destructive) { deleteAccount() }
        } message: {
            Text("Esta acción es irreversible. ¿Quieres eliminar tu cuenta?")
        }
        .alert(errorMessage ?? "", isPresented: Binding(get: { errorMessage != nil }, set: { _ in errorMessage = nil })) {
            Button("OK", role: .cancel) { }
        }
    }

    private func logout() {
        isProcessing = true
        SecureStorage.shared.clear { error in
            DispatchQueue.main.async {
                userVM.clearCurrentUser()
                appState.isAuthenticated = false
                isProcessing = false
            }
        }
    }

    private func deleteAccount() {
        guard let email = userVM.currentUser?.email else { return }
        isProcessing = true
        userVM.deleteUser(email: email) { success, error in
            if success {
                logout()
            } else {
                errorMessage = error ?? "Error desconocido"
                isProcessing = false
            }
        }
    }
}

#Preview {
    NavigationStack {
        ConfigurationView().environmentObject(UserViewModel()).environmentObject(AppState())
    }
}
