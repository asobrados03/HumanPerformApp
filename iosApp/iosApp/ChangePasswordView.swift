import SwiftUI

struct ChangePasswordView: View {
    @EnvironmentObject var userVM: UserViewModel
    @StateObject private var authVM = AuthViewModel()

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

            if case let .error(message) = authVM.changePasswordState {
                Text(message)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            Button(action: {
                authVM.resetChangePasswordState()
                if let id = userVM.currentUser?.id {
                    authVM.changePassword(
                        current: currentPassword,
                        new: newPassword,
                        confirm: confirmPassword,
                        userId: id
                    )
                }
            }) {
                if case .loading = authVM.changePasswordState {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Guardar cambios")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(authVM.changePasswordState == .loading)

            Spacer()
        }
        .padding(16)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .onChange(of: authVM.changePasswordState) { state in
            if case .success = state {
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
}

#Preview {
    NavigationStack {
        ChangePasswordView().environmentObject(UserViewModel())
    }
}
