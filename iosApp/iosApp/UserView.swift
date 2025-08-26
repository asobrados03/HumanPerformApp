//
//  UserView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct UserView: View {
    var body: some View {
        VStack(spacing: 16) {
            Text("Aqui se mostrara la info del usuario")
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}
