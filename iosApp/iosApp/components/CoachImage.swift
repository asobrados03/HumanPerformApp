//
//  CoachImage.swift
//  iosApp
//
//  Created by user294332 on 4/2/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI
import shared

struct CoachImage: View {

    let photoName: String?
    let isSelected: Bool
    let avatarSize: CGFloat

    private var imageURL: URL? {
        guard let photoName,
              let encoded = photoName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)
        else {
            return nil
        }

        let base = "\(HttpClientProviderKt.API_BASE_URL)/profile_pic/"
        return URL(string: base + encoded)
    }

    private var circleStroke: Color {
        isSelected ? Color.white.opacity(0.7) : Color.secondary.opacity(0.25)
    }

    var body: some View {
        Group {
            if let imageURL {
                AsyncImage(url: imageURL) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(width: avatarSize, height: avatarSize)
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(width: avatarSize, height: avatarSize)
                            .clipShape(Circle())
                            .overlay(Circle().stroke(circleStroke, lineWidth: 1))
                    default:
                        placeholder
                    }
                }
            } else {
                placeholder
            }
        }
    }

    private var placeholder: some View {
        ZStack {
            Circle()
                .fill(isSelected ? Color.white.opacity(0.18) : Color(.systemGray5))

            Image(systemName: "person.fill")
                .imageScale(.medium)
                .foregroundColor(isSelected ? .white : .secondary)
        }
        .frame(width: avatarSize, height: avatarSize)
        .overlay(Circle().stroke(circleStroke, lineWidth: 1))
    }
}
