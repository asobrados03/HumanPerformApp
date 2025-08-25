import SwiftUI
import shared

// Enum de estados posibles del proceso de login
enum LoginState: Equatable {
    case idle
    case loading
    case validationErrors([LoginField: String])
    case success
    case error(message: String)
}

struct LoginView: View {
    @StateObject var vm = AuthViewModel()

    var onSuccess: (() -> Void)? = nil
    var onForgot: (() -> Void)? = nil
    var onRegister: (() -> Void)? = nil

    @State private var passwordVisible = false

    var body: some View {
        VStack(spacing: 16) {
            Text("Accede a tu cuenta")
                .font(.title2).fontWeight(.semibold)
                .padding(.top, 8)

            // Email
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    Image(systemName: "envelope")
                    TextField("Correo electrónico", text: $vm.loginEmail)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                        .onChange(of: vm.loginEmail) { _ in
                            // el VM ya limpia errores en didSet -> clearLoginErrorsIfNeeded()
                        }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 14)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.gray.opacity(0.35), lineWidth: 1)
                )

                // Error específico de email
                if case .validationErrors(let errs) = vm.loginState,
                   let msg = errs[.email], !msg.isEmpty {
                    Text(msg)
                        .font(.footnote)
                        .foregroundColor(.red)
                }
            }

            // Password
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    Image(systemName: "lock")

                    Group {
                        if passwordVisible {
                            TextField("Contraseña", text: $vm.loginPassword)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled(true)
                        } else {
                            SecureField("Contraseña", text: $vm.loginPassword)
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
                .onChange(of: vm.loginPassword) { _ in
                    // el VM ya limpia errores en didSet -> clearLoginErrorsIfNeeded()
                }

                // Error específico de password
                if case .validationErrors(let errs) = vm.loginState,
                   let msg = errs[.password], !msg.isEmpty {
                    Text(msg)
                        .font(.footnote)
                        .foregroundColor(.red)
                }
            }

            // Error general (backend / red)
            if case .error(let msg) = vm.loginState {
                Text(msg)
                    .font(.subheadline)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 4)
            }

            // Botón Login
            Button {
                vm.login() // ahora login no es async/await
            } label: {
                HStack {
                    if case .loading = vm.loginState {
                        ProgressView().tint(.white)
                    }
                    Text("Iniciar sesión").fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
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
        .navigationBarTitleDisplayMode(.inline) // importante para centrar el contenido del .principal
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo() 
            }
        }
        .onChange(of: vm.loginState) { newValue in
            if case .success = newValue {
                onSuccess?()
                // Si quieres limpiar tras navegar:
                vm.resetStates()
            }
        }
    }

    private var isLoading: Bool {
        if case .loading = vm.loginState { return true }
        return false
    }
}

// Preview
#Preview {
    NavigationStack {
        LoginView(
            onSuccess: { print("Ir a principal") },
            onForgot: { print("Recuperar contraseña") },
            onRegister: { print("Registro") }
        )
    }
}
