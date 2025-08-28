import SwiftUI

struct PasswordResetInfoView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Revisa la bandeja de entrada de tu correo electrónico, allí habrás recibido una nueva contraseña.")
            Text("Una vez hayas iniciado sesión, tendrás que cambiar la contraseña generada automáticamente por la tuya personal.")
            Text("Para cambiar la contraseña dirígete a:")
            Text("Usuario > Configuración > Cambiar contraseña")
                .fontWeight(.bold)
        }
        .padding(16)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { NavBarLogo() }
        }
    }
}

#Preview {
    NavigationStack {
        PasswordResetInfoView()
    }
}
