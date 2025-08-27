import SwiftUI

/// Versión editable de `UserProfileImageView` con un botón de cambio.
struct EditableUserProfileImageView: View {
    let photoName: String?
    let image: UIImage?
    let onTap: () -> Void

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            UserProfileImageView(photoName: photoName, image: image)
            Image(systemName: "plus.circle.fill")
                .foregroundColor(.blue)
                .background(Color.white.clipShape(Circle()))
                .offset(x: 4, y: 4)
        }
        .onTapGesture { onTap() }
    }
}
