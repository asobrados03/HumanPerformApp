import SwiftUI
import KMPObservableViewModelSwiftUI
import shared

struct ChangePasswordView: View {
    @StateViewModel private var sessionVM = SharedDependencies.shared.makeUserSessionViewModel()
    @StateViewModel private var authVM = SharedDependencies.shared.makeAuthViewModel()

    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""
    @State private var showCurrent = false
    @State private var showNew = false
    @State private var showConfirm = false
    @State private var showSuccess = false

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 16) {
            Text("Cambiar contraseña")
                .font(.title2)
                .fontWeight(.semibold)
                .padding(.bottom, 24)

            passwordField("Contraseña actual", text: $currentPassword, visible: $showCurrent)
            passwordField("Nueva contraseña", text: $newPassword, visible: $showNew)
            passwordField("Confirmar nueva contraseña", text: $confirmPassword, visible: $showConfirm)

            if let errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            Button(action: {
                authVM.resetChangePasswordState()
                if let id = sessionVM.userData?.id {
                    authVM.changePassword(
                        currentPassword: currentPassword,
                        newPassword: newPassword,
                        confirmPassword: confirmPassword,
                        userId: id
                    )
                }
            }) {
                if isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Guardar cambios")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(isLoading)

            Spacer()
        }
        .padding(16)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onChange(of: authVM.isChangingPassword) { state in
            if isSuccessState(state) {
                showSuccess = true
            }
        }
        .alert("Contraseña cambiada", isPresented: $showSuccess) {
            Button("OK") { dismiss() }
        } message: {
            Text("Tu contraseña ha sido actualizada.")
        }
    }

    @ViewBuilder
    private func passwordField(_ title: String, text: Binding<String>, visible: Binding<Bool>) -> some View {
        HStack {
            if visible.wrappedValue {
                TextField(title, text: text)
                    .autocapitalization(.none)
            } else {
                SecureField(title, text: text)
                    .autocapitalization(.none)
            }
            Button(action: { visible.wrappedValue.toggle() }) {
                Image(systemName: visible.wrappedValue ? "eye.slash" : "eye")
                    .foregroundColor(.gray)
            }
        }
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.4)))
    }

    private var isLoading: Bool {
        isState(authVM.isChangingPassword, named: "Loading")
    }

    private var errorMessage: String? {
        guard isState(authVM.isChangingPassword, named: "Error") else { return nil }
        return Mirror(reflecting: authVM.isChangingPassword)
            .children
            .first(where: { $0.label == "message" })?
            .value as? String
    }

    private func isSuccessState(_ state: Any) -> Bool {
        isState(state, named: "Success")
    }

    private func isState(_ state: Any, named suffix: String) -> Bool {
        String(describing: type(of: state)).hasSuffix(suffix)
    }
}

#Preview {
    NavigationStack {
        ChangePasswordView()
    }
}
