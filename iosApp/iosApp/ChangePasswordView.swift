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
    @State private var fieldErrors: [PasswordField: String] = [:]
    @State private var globalErrorMessage: String?

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

            if let globalErrorMessage {
                Text(globalErrorMessage)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            } else if let errorMessage {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            Button(action: {
                authVM.resetChangePasswordState()
                globalErrorMessage = nil
                fieldErrors = [:]

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
            if isErrorState(state) {
                applyErrorFeedback(message: errorMessage)
            }
        }
        .onChange(of: currentPassword) { _ in
            clearError(for: .currentPassword)
        }
        .onChange(of: newPassword) { _ in
            clearError(for: .newPassword)
            if confirmPassword == newPassword {
                clearError(for: .confirmPassword)
            }
        }
        .onChange(of: confirmPassword) { _ in
            clearError(for: .confirmPassword)
        }
        .alert("Contraseña cambiada", isPresented: $showSuccess) {
            Button("OK") { dismiss() }
        } message: {
            Text("Tu contraseña ha sido actualizada.")
        }
    }

    @ViewBuilder
    private func passwordField(_ title: String, text: Binding<String>, visible: Binding<Bool>) -> some View {
        let field = passwordField(for: title)
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
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(fieldErrors[field] == nil ? Color.gray.opacity(0.4) : .red, lineWidth: 1)
        )
        .overlay(alignment: .bottomLeading) {
            if let message = fieldErrors[field] {
                Text(message)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.top, 52)
            }
        }
        .padding(.bottom, fieldErrors[field] == nil ? 0 : 16)
    }

    private var isLoading: Bool {
        isState(authVM.isChangingPassword, named: "Loading")
    }

    private var errorMessage: String? {
        guard isState(authVM.isChangingPassword, named: "Error") else { return nil }
        if let direct = propertyValue(named: "message", from: authVM.isChangingPassword) as? String {
            return direct
        }
        if let nested = nestedValue(from: authVM.isChangingPassword),
           let nestedMessage = propertyValue(named: "message", from: nested) as? String {
            return nestedMessage
        }
        if let nested = nestedValue(from: authVM.isChangingPassword) {
            return String(describing: nested)
        }
        return nil
    }

    private func isSuccessState(_ state: Any) -> Bool {
        isState(state, named: "Success")
    }

    private func isErrorState(_ state: Any) -> Bool {
        isState(state, named: "Error")
    }

    private func isState(_ state: Any, named suffix: String) -> Bool {
        String(describing: type(of: state)).hasSuffix(suffix)
    }

    private func clearError(for field: PasswordField) {
        fieldErrors[field] = nil
        globalErrorMessage = nil
    }

    private func applyErrorFeedback(message: String?) {
        guard let message = message?.trimmingCharacters(in: .whitespacesAndNewlines), !message.isEmpty else {
            globalErrorMessage = "No se pudo cambiar la contraseña."
            return
        }

        if let field = mapErrorToField(message: message) {
            fieldErrors[field] = message
            globalErrorMessage = nil
        } else {
            globalErrorMessage = message
        }
    }

    private func mapErrorToField(message: String) -> PasswordField? {
        let normalized = message.folding(options: .diacriticInsensitive, locale: .current).lowercased()

        if normalized.contains("actual") {
            return .currentPassword
        }

        if normalized.contains("confirm") || normalized.contains("coincid") {
            return .confirmPassword
        }

        if normalized.contains("nueva")
            || normalized.contains("mayuscula")
            || normalized.contains("minuscula")
            || normalized.contains("numero")
            || normalized.contains("8 caracteres")
            || normalized.contains("diferente a la actual")
            || normalized.contains("espacios") {
            return .newPassword
        }

        return nil
    }

    private func passwordField(for title: String) -> PasswordField {
        switch title {
        case "Contraseña actual": return .currentPassword
        case "Nueva contraseña": return .newPassword
        default: return .confirmPassword
        }
    }

    private func propertyValue(named label: String, from state: Any) -> Any? {
        Mirror(reflecting: state).children.first(where: { $0.label == label })?.value
    }

    private func nestedValue(from state: Any) -> Any? {
        Mirror(reflecting: state).children.first?.value
    }

    private enum PasswordField: Hashable {
        case currentPassword
        case newPassword
        case confirmPassword
    }
}

#Preview {
    NavigationStack {
        ChangePasswordView()
    }
}
