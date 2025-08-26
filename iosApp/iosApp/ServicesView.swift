//
//  ServicesView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

enum ServicesSection: Int, CaseIterable, Identifiable {
    case myProducts = 0, hire
    var id: Int { rawValue }
    var title: String { self == .myProducts ? "Mis productos" : "Contratar" }
}

struct ServicesView: View {
    @SceneStorage("services.selected") private var selectedRaw = ServicesSection.myProducts.rawValue
    private var selected: Binding<ServicesSection> {
        Binding(
            get: { ServicesSection(rawValue: selectedRaw) ?? .myProducts },
            set: { selectedRaw = $0.rawValue }
        )
    }

    // 👇 inyección clara
    init(initial: ServicesSection = .myProducts) {
        _selectedRaw = SceneStorage(wrappedValue: initial.rawValue, "services.selected")
    }

    var body: some View {
        VStack {
            Picker("Seleccione sección", selection: selected) {
                ForEach(ServicesSection.allCases) { s in
                    Text(s.title).tag(s)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)

            switch selected.wrappedValue {
            case .myProducts: MyProductsView()
            case .hire:       HireServicesView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}
