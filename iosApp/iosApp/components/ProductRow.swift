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
    let finalPrice: Double
    let isHired: Bool
    let matchAndroidStyle: Bool

    init(
        producto: Product,
        finalPrice: Double,
        isHired: Bool,
        matchAndroidStyle: Bool = false
    ) {
        self.producto = producto
        self.finalPrice = finalPrice
        self.isHired = isHired
        self.matchAndroidStyle = matchAndroidStyle
    }

    private var productImageURL: URL? {
        guard let imageName = producto.image,
              let encoded = imageName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
        else {
            return nil
        }

        return URL(string: "\(HttpClientProviderKt.API_BASE_URL)/product_images/\(encoded)")
    }

    var body: some View {
        HStack(alignment: .center) {
            if let productImageURL {
                AsyncImage(url: productImageURL) { image in
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

                if isHired && !matchAndroidStyle {
                    Text("Ya contratado")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 4) {
                if finalPrice < (producto.price?.doubleValue ?? 0) && !matchAndroidStyle {
                    Text(String(format: "%.2f€", producto.price?.doubleValue ?? 0))
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .strikethrough()
                }

                Text(String(format: "%.2f€", finalPrice))
                    .fontWeight(matchAndroidStyle ? .bold : (isHired ? .regular : .bold))
                    .foregroundColor(matchAndroidStyle ? .red : (isHired ? .secondary : .red))

                if isHired && !matchAndroidStyle {
                    Text("Adquirido")
                        .font(.caption2)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.green.opacity(0.15))
                        .foregroundColor(Color.green)
                        .clipShape(Capsule())
                }
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .stroke(
                    matchAndroidStyle ? Color(UIColor.separator) : (isHired ? Color.green.opacity(0.7) : Color.secondary),
                    lineWidth: 1
                )
        )
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(
                    matchAndroidStyle ? Color(UIColor.systemBackground) : (isHired ? Color.green.opacity(0.06) : Color(UIColor.systemBackground))
                )
        )
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 1)
        .padding(.vertical, 4)
    }
}
