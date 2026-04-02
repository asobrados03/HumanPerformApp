//
//  CoachRow.swift
//  iosApp
//
//  Created by user294332 on 4/2/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import SwiftUI

struct CoachRow: View {

    let coach: CoachUI
    let isSelected: Bool
    let avatarSize: CGFloat
    let selectedColor: Color
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            CoachImage(
                photoName: coach.photoName,
                isSelected: isSelected,
                avatarSize: avatarSize
            )

            Text(coach.name)
                .font(.body)
                .foregroundStyle(isSelected ? Color.white : .primary)
                .lineLimit(1)
                .truncationMode(.tail)
        }
        .padding(.vertical, 8)
        .contentShape(Rectangle())
        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 12))
        .listRowBackground(isSelected ? selectedColor : Color(.systemBackground))
        .onTapGesture(perform: onTap)
    }
}
