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

    let coach: Professional
    let isSelected: Bool
    let avatarSize: CGFloat

    var body: some View {
        let base = "\(HttpClientProviderKt.API_BASE_URL)/profile_pic/"
        let circleStroke = isSelected ? Color.white.opacity(0.7) : Color.secondary.opacity(0.25)

        Group {
            if let photo = coach.photoName,
               let encoded = photo.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
               let url = URL(string: base + encoded) {

                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(width: avatarSize, height: avatarSize)

                    case .success(let img):
                        img.resizable()
                            .scaledToFill()
                            .frame(width: avatarSize, height: avatarSize)
                            .clipShape(Circle())
                            .overlay(
                                Circle().stroke(circleStroke, lineWidth: 1)
                            )

                    default:
                        placeholder(circleStroke: circleStroke)
                    }
                }

            } else {
                placeholder(circleStroke: circleStroke)
            }
        }
    }

    private func placeholder(circleStroke: Color) -> some View {
        ZStack {
            Circle()
                .fill(isSelected ? Color.white.opacity(0.18) : Color(.systemGray5))

            Image(systemName: "person.fill")
                .imageScale(.medium)
                .foregroundColor(isSelected ? .white : .secondary)
        }
        .frame(width: avatarSize, height: avatarSize)
        .overlay(
            Circle().stroke(circleStroke, lineWidth: 1)
        )
    }
}
