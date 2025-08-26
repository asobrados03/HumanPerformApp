//
//  CalendarView.swift
//  iosApp
//
//  Created by user284952 on 8/25/25.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI

struct CalendarView: View {
    var body: some View {
        VStack(spacing: 16) {
            Text("Aqui se mostrara el calendario")
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
    }
}
