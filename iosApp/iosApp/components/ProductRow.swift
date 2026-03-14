//
//  ProductRow.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import Foundation
import shared

struct ProductRow: View {
    let producto: Product
    var body: some View {
        HStack(alignment: .center) {
            if let imageName = producto.image {
                // Cargar imagen desde URL (se puede usar AsyncImage de SwiftUI en iOS 15+)
                AsyncImage(url: URL(string: "https://apihuman.fransdata.com/api/product_images/\(imageName)")) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.gray.opacity(0.2)
                }
                .frame(width: 69, height: 69)
                .cornerRadius(8)
                .padding(.trailing, 12)
            }
            VStack(alignment: .leading) {
                Text(producto.name)
                    .fontWeight(.bold)
                // Aquí podríamos añadir más info si hubiera
            }
            Spacer()
            Text(String(format: "%.2f€", producto.price?.doubleValue ?? 0))
                .fontWeight(.bold)
                .foregroundColor(.red)
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.secondary, lineWidth: 1)
        )
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(Color(UIColor.systemBackground))  // fondo card (blanco) adaptado a modo oscuro/claro
        )
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
        .padding(.vertical, 4)
    }
}
