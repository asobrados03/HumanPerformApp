//
//  ServiceRow.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ServiceRow: View {
    let servicio: ServiceAvailable
    let contratado: Bool
    var body: some View {
        HStack(alignment: .center) {
            if let imageName = servicio.image {
                AsyncImage(url: URL(string: "https://apihuman.fransdata.com/api/service_images/\(imageName)")) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.gray.opacity(0.2)
                }
                .frame(width: 69, height: 69)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .background(Color.white)
                .padding(4)    // padding interno blanco como en Android card:contentReference[oaicite:70]{index=70}
                .cornerRadius(8)
            }
            VStack(alignment: .leading) {
                Text(servicio.name).fontWeight(.bold)
                if contratado {
                    Text("Ya tienes productos de este servicio")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                }
            }
            .padding(.leading, 8)
            Spacer()
            Image(systemName: "chevron.right")  // flecha indicativa
                .foregroundColor(.secondary)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.secondary, lineWidth: 1)
        )
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(UIColor.systemBackground))
        )
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
        .padding(.vertical, 4)
    }
}

