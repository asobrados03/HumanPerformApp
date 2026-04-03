import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct LoginView: View {
    @StateViewModel var vm = SharedDependencies.shared.makeAuthViewModel()

    var onSuccess: (() -> Void)? = nil
    var onForgot: (() -> Void)? = nil
    var onRegister: (() -> Void)? = nil

    @State private var passwordVisible = false
    @State private var email = ""
    @State private var password = ""
    @State private var uiTestError: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Accede a tu cuenta")
                .font(.title2).fontWeight(.semibold)
                .padding(.top, 8)

            // Email
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    Image(systemName: "envelope")
                    TextField("Correo electrónico", text: $email)
                        .accessibilityIdentifier("loginEmailField")
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 14)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.gray.opacity(0.35), lineWidth: 1)
                )
            }

            // Password
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    Image(systemName: "lock")

                    Group {
                        if passwordVisible {
                            TextField("Contraseña", text: $password)
                                .accessibilityIdentifier("loginPasswordField")
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled(true)
                        } else {
                            SecureField("Contraseña", text: $password)
                                .accessibilityIdentifier("loginPasswordField")
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled(true)
                        }
                    }

                    Button(action: { passwordVisible.toggle() }) {
                        Image(systemName: passwordVisible ? "eye.slash" : "eye")
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 14)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.gray.opacity(0.35), lineWidth: 1)
                )
            }

            // Error general (backend / red)
            if let uiTestError {
                Text(uiTestError)
                    .font(.subheadline)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 4)
                    .accessibilityIdentifier("loginErrorMessage")
            } else if let message = errorMessage {
                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 4)
            }

            // Botón Login
            Button {
                handleLoginTap()
            } label: {
                HStack {
                    if isLoading {
                        ProgressView().tint(.white)
                    }
                    Text("Iniciar sesión").fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .accessibilityIdentifier("loginSubmitButton")
            .disabled(isLoading)
            .buttonStyle(.borderedProminent)
            .tint(.accentColor)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .padding(.top, 8)

            // Enlaces
            VStack(spacing: 8) {
                Button("¿Olvidaste tu contraseña?") { onForgot?() }
                    .buttonStyle(.plain)
                    .foregroundColor(.accentColor)

                HStack(spacing: 4) {
                    Text("¿No tienes cuenta?")
                    Button("Regístrate ya") { onRegister?() }
                        .buttonStyle(.plain)
                        .foregroundColor(.accentColor)
                }
                .font(.subheadline)
            }
            .padding(.top, 8)

            Spacer()
        }
        .padding(.horizontal, 24)
        .padding(.top, 12)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo()
            }
        }
        .accessibilityIdentifier("loginView")
        .onChange(of: vm.loginState) { newValue in
            if newValue is LoginState.Success {
                onSuccess?()
                vm.resetStates()
            }
        }
    }

    // MARK: - Computed properties

    private var isLoading: Bool {
        vm.loginState is LoginState.Loading
    }

    private var errorMessage: String? {
        (vm.loginState as? LoginState.Error)?.message
    }

    // MARK: - Helpers

    private func handleLoginTap() {
        if UITestConfig.isUITesting && UITestConfig.forceAuthError {
            uiTestError = "Credenciales inválidas (mock)"
            return
        }

        if UITestConfig.isUITesting {
            uiTestError = nil
            onSuccess?()
            return
        }

        vm.login(email: email, password: password)
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        LoginView(
            onSuccess: { print("Ir a principal") },
            onForgot: { print("Recuperar contraseña") },
            onRegister: { print("Registro") }
        )
    }
}
