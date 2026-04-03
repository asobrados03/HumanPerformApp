import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

/// Pantalla de configuración del usuario.
struct ConfigurationView: View {
    @EnvironmentObject var appState: AppState
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()

    @State private var showLogoutAlert = false
    @State private var showDeleteAlert = false
    @State private var errorMessage: String?

    private var isProcessing: Bool {
        sessionVM.isLoggingOut || resolvedDeleteStateKind == "loading"
    }

    private var deleteStateChangeKey: String {
        "\(resolvedDeleteStateKind):\(resolvedDeleteStateMessage ?? "")"
    }

    /// Evita depender de nombres de clases generadas por Kotlin/Native
    /// (por ejemplo, `DeleteUserStateLoading`) que cambian entre versiones.
    private var resolvedDeleteStateKind: String {
        let kind = sessionVM.deleteStateKind()
        if !kind.isEmpty {
            return kind
        }

        let rawState = String(describing: sessionVM.deleteState)
        if rawState.localizedCaseInsensitiveContains("Loading") { return "loading" }
        if rawState.localizedCaseInsensitiveContains("Success") { return "success" }
        if rawState.localizedCaseInsensitiveContains("NotFound") { return "notFound" }
        if rawState.localizedCaseInsensitiveContains("Error") { return "error" }
        return "idle"
    }

    private var resolvedDeleteStateMessage: String? {
        let message = sessionVM.deleteStateMessage()
        if let message, !message.isEmpty {
            return message
        }

        if resolvedDeleteStateKind == "error" {
            return "Error desconocido"
        }

        return nil
    }

    var body: some View {
        VStack(spacing: 16) {
            Text("Configuración")
                .font(.title2)
                .fontWeight(.semibold)
        }

        ZStack {
            List {
                Section {
                    if let user = sessionVM.userData {
                        Text(user.fullName)
                            .font(.headline)
                    }
                    Button("Cerrar sesión") { showLogoutAlert = true }
                    Button("Eliminar cuenta") { showDeleteAlert = true }
                        .foregroundColor(.red)
                    NavigationLink("Cambiar contraseña") {
                        ChangePasswordView()
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
        .onChange(of: deleteStateChangeKey) { _ in
            switch resolvedDeleteStateKind {
            case "success":
                appState.isAuthenticated = false
                sessionVM.resetDeleteState()
            case "notFound", "error":
                errorMessage = resolvedDeleteStateMessage ?? "Error desconocido"
                sessionVM.resetDeleteState()
            default:
                break
            }
        }
    }

    private func logout() {
        sessionVM.logout {
            DispatchQueue.main.async {
                appState.isAuthenticated = false
            }
        }
    }

    private func deleteAccount() {
        guard let email = sessionVM.userData?.email else {
            errorMessage = "No se pudo determinar el email de la cuenta."
            return
        }
        sessionVM.deleteUser(email: email)
    }
}

#Preview {
    NavigationStack {
        ConfigurationView().environmentObject(AppState())
    }
}
