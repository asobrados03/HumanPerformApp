import SwiftUI
import shared
import KMPObservableViewModelSwiftUI

struct EnterEmailView: View {
    @State private var email: String = ""
    @State private var isEmailValid: Bool = true
    @StateViewModel private var vm = SharedDependencies.shared.makeAuthViewModel()

    var onSuccess: () -> Void = {}
    @State private var errorMessage: String?
    @State private var showError = false

    var body: some View {
        VStack(spacing: 16) {
            Text("Introduce tu correo electrónico para restablecer la contraseña")
                .multilineTextAlignment(.center)

            HStack(spacing: 10) {
                Image(systemName: "envelope")
                TextField("Correo electrónico", text: $email)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled(true)
                    .onChange(of: email) { newValue in
                        isEmailValid = isValidEmail(newValue)
                    }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 14)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color.gray.opacity(0.35), lineWidth: 1)
            )
            
            if !isEmailValid {
                Text("Introduce un correo válido")
                    .font(.footnote)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Button {
                if isEmailValid {
                    vm.resetPassword(email: email)
                }
            } label: {
                HStack {
                    if case .loading = vm.resetPasswordState {
                        ProgressView().tint(.white)
                    }
                    Text("Enviar").fontWeight(.semibold)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
            }
            .disabled(email.isEmpty || !isEmailValid || isLoading)
            .buttonStyle(.borderedProminent)
            .tint(.accentColor)
            .clipShape(RoundedRectangle(cornerRadius: 12))

            Spacer()
        }
        .padding(.horizontal, 24)
        .padding(.top, 12)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { NavBarLogo() }
        }
        .alert(errorMessage ?? "", isPresented: $showError) {
            Button("OK", role: .cancel) {}
        }
        .onChange(of: vm.resetPasswordState) { newValue in
            switch newValue {
            case .success:
                onSuccess()
                vm.resetResetPasswordState()
            case .error(let msg):
                errorMessage = msg
                showError = true
                vm.resetResetPasswordState()
            default:
                break
            }
        }
    }

    private var isLoading: Bool {
        if case .loading = vm.resetPasswordState { return true }
        return false
    }

    private func isValidEmail(_ email: String) -> Bool {
        let emailRegex = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return NSPredicate(format: "SELF MATCHES %@", emailRegex).evaluate(with: email)
    }
}

#Preview {
    NavigationStack {
        EnterEmailView()
    }
}
