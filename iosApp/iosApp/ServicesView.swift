//
//  ServicesView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

struct ServicesView: View {
    @State private var selectedSegment = 0
    let segmentTitles = ["Mis productos", "Contratar"]
    
    var body: some View {
        VStack {
            Picker("Seleccione sección", selection: $selectedSegment) {
                Text("Mis productos").tag(0)
                Text("Contratar").tag(1)
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding(.horizontal)
                    
            if selectedSegment == 0 {
                MyProductsView()
            } else {
                HireServicesView()
            }
        }
        .navigationBarTitleDisplayMode(.inline) // importante para centrar el contenido del .principal
        .toolbar {
            ToolbarItem(placement: .principal) {
                NavBarLogo() // o NavBarLogo(name: "otro_asset", height: 24)
            }
        }
        
        BottomNavBar()
        
    }
}
