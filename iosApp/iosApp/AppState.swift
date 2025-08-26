//
//  AppState.swift
//  iosApp
//
//  Created by user284952 on 8/26/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI

final class AppState: ObservableObject {
    @Published var isAuthenticated: Bool = false
}
