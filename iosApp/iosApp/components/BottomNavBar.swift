//
//  BottomNavBar.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct BottomNavBar: View {
    var body: some View{
        TabView {
            ServicesView()  // vista "Producto"
                .tabItem {
                    Label("Producto", systemImage: "figure.walk")  // usar SF Symbol similar a ejercicio
                }
            CalendarView()
                .tabItem {
                    Label("Calendario", systemImage: "calendar")
                }
            StatsView()
                .tabItem {
                    Label("Estadísticas", systemImage: "chart.bar")
                }
            UserView()
                .tabItem {
                    Label("Usuario", systemImage: "person.circle")
                }
        }

    }
}
