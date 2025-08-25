//
//  NavBarLogo.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct NavBarLogo: View {
    var name: String = "colored_logo"
    var height: CGFloat = 22

    var body: some View {
        Image(name)
            .resizable()
            .scaledToFit()
            .frame(height: height)
            .accessibilityLabel("Human Perform")
            .accessibilityAddTraits(.isHeader)
    }
}
