import SwiftUI
import Foundation
import shared

/// Muestra la foto de perfil del usuario desde el servidor o una imagen local seleccionada.
struct UserProfileImageView: View {
    /// Nombre del archivo en el backend (p.ej. "avatar.jpg")
    let photoName: String?
    /// Imagen local seleccionada (cuando el usuario cambia la foto)
    let image: UIImage?
    var body: some View {
        Group {
            if let image = image {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
            } else if let photoName = photoName,
                      let encoded = photoName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
                      let url = URL(string: "\(ApiClient.shared.baseUrl)/profile_pic/\(encoded)") {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                    case .success(let img):
                        img.resizable().scaledToFill()
                    case .failure:
                        placeholder
                    @unknown default:
                        placeholder
                    }
                }
            } else {
                placeholder
            }
        }
        .frame(width: 80, height: 80)
        .clipShape(Circle())
        .overlay(Circle().stroke(Color.white, lineWidth: 2))
    }

    private var placeholder: some View {
        Image(systemName: "person.crop.circle.fill")
            .resizable()
            .scaledToFill()
            .foregroundColor(.gray.opacity(0.6))
    }
}
