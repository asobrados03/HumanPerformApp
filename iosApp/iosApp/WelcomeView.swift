import SwiftUI

struct WelcomeView: View {
    var body: some View {
        VStack(spacing: 20) {
            // Logo
            Image("colored_logo")
                .resizable()
                .scaledToFit()
                .frame(width: 300, height: 300)

            // Botón Registro → va a RegisterView
            NavigationLink(destination: RegisterView()) {
                GradientPill(title: "Registro")
            }

            // Botón Acceso → va a LoginView
            NavigationLink(destination: LoginView()) {
                GradientPill(title: "Acceso")
            }
        }
        .padding(.horizontal, 32)
    }
}

struct GradientPill: View {
    let title: String
    private let gradientColors = [
        Color(red: 0x6D/255, green: 0x2A/255, blue: 0x6F/255),
        Color(red: 0xEF/255, green: 0x0E/255, blue: 0x29/255)
    ]

    var body: some View {
        Text(title)
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(
                LinearGradient(
                    colors: gradientColors,
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(Capsule())
            .contentShape(Capsule())
    }
}
