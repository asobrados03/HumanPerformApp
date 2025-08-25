//
//  ServicesView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct ServicesView: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                Text("Has accedido a la app.")
            }
            .padding(.horizontal, 16)
        }
        .navigationBarTitleDisplayMode(.inline) // importante para centrar el contenido del .principal
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo() // o NavBarLogo(name: "otro_asset", height: 24)
            }
        }
    }
}
