import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

/// Pantalla de configuración del usuario.
struct ConfigurationView: View {
    @EnvironmentObject var appState: AppState
    @StateViewModel private var sessionVM = makeUserSessionViewModel()

    @State private var showLogoutAlert = false
    @State private var showDeleteAlert = false
    @State private var errorMessage: String?

    private var isProcessing: Bool {
        sessionVM.isLoggingOut || sessionVM.deleteState is DeleteUserStateLoading
    }

    private var deleteStateChangeKey: String {
        switch sessionVM.deleteState {
        case is DeleteUserStateIdle:
            return "idle"
        case is DeleteUserStateLoading:
            return "loading"
        case is DeleteUserStateSuccess:
            return "success"
        case let notFound as DeleteUserStateNotFound:
            return "notFound:\(notFound.email)"
        case let error as DeleteUserStateError:
            return "error:\(error.message)"
        default:
            return "unknown"
        }
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
                        ChangePasswordView().environmentObject(sessionVM)
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
            switch sessionVM.deleteState {
            case is DeleteUserStateSuccess:
                appState.isAuthenticated = false
                sessionVM.resetDeleteState()
            case let notFound as DeleteUserStateNotFound:
                errorMessage = "No se encontró la cuenta para \(notFound.email)."
                sessionVM.resetDeleteState()
            case let error as DeleteUserStateError:
                errorMessage = error.message
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
