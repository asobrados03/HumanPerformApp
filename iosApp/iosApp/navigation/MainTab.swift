import SwiftUI

enum MainTab: Hashable { case services, calendar, stats, user }
enum ServicesRoute: Hashable {
    case myProducts
    case hireService
    case hireProducts(serviceId: Int)
    case productDetail(productId: Int)
    case paymentSuccess
}

struct MainTabs: View {
    @State private var selected: MainTab = .services
    @State private var servicesPath: [ServicesRoute] = []

    var body: some View {
        TabView(selection: $selected) {
            NavigationStack(path: $servicesPath) {
                ServicesView(
                    initial: .myProducts,
                    onOpenHireProducts: { serviceId in
                        servicesPath.append(.hireProducts(serviceId: serviceId))
                    },
                    onOpenProductDetail: { productId in
                        servicesPath.append(.productDetail(productId: productId))
                    }
                )
                .navigationDestination(for: ServicesRoute.self) { route in
                    switch route {
                    case .myProducts:
                        ServicesView(initial: .myProducts)
                    case .hireService:
                        ServicesView(initial: .hire)
                    case .hireProducts(let serviceId):
                        HireProductsView(
                            serviceId: serviceId,
                            onOpenProductDetail: { productId in
                                servicesPath.append(.productDetail(productId: productId))
                            }
                        )
                    case .productDetail(let productId):
                        ProductDetailView(
                            productId: productId,
                            onPaymentSuccess: {
                                servicesPath.append(.paymentSuccess)
                            }
                        )
                    case .paymentSuccess:
                        PaymentSuccessView {
                            servicesPath = []
                            selected = .services
                        }
                    }
                }
                .navigationTitle("Servicios")
                .navigationBarTitleDisplayMode(.inline)
                .accessibilityIdentifier("servicesNavRoot")
            }
            .tabItem { Label("Producto", systemImage: "bag") }
            .accessibilityIdentifier("tabServices")
            .tag(MainTab.services)

            NavigationStack {
                CalendarView()
                    .accessibilityIdentifier("calendarView")
                    .navigationTitle("Calendario")
            }
            .tabItem { Label("Calendario", systemImage: "calendar") }
            .accessibilityIdentifier("tabCalendar")
            .tag(MainTab.calendar)

            NavigationStack {
                StatsView()
                    .accessibilityIdentifier("statsView")
                    .navigationTitle("Estadísticas")
            }
            .tabItem { Label("Estadísticas", systemImage: "chart.bar") }
            .accessibilityIdentifier("tabStats")
            .tag(MainTab.stats)

            NavigationStack {
                UserView()
                    .accessibilityIdentifier("userView")
                    .navigationTitle("Usuario")
            }
            .tabItem { Label("Usuario", systemImage: "person.circle") }
            .accessibilityIdentifier("tabUser")
            .tag(MainTab.user)
        }
        .toolbarBackground(.visible, for: .tabBar)
        .toolbarBackground(Color(.systemBackground), for: .tabBar)
    }

    func openServicesMyProducts() {
        selected = .services
        servicesPath = [.myProducts]
    }

    func openServicesHire() {
        selected = .services
        servicesPath = [.hireService]
    }
}
//  Created by user284952 on 8/26/25.//  Copyright © 2025 orgName. All rights reserved.//import SwiftUIenum MainTab: Hashable { case services, calendar, stats, user }enum ServicesRoute: Hashable { case myProducts, hireService }struct MainTabs: View {    @State private var selected: MainTab = .services    // ⬇️ paths vacíos    @State private var servicesPath: [ServicesRoute] = []    // Si no navegas dentro de estos tabs, ni siquiera necesitas path    // Los dejo sin path para simplificar.        var body: some View {        TabView(selection: $selected) {            // SERVICES            NavigationStack(path: $servicesPath) {                // Raíz del tab: muestra directamente la vista con el segmento por defecto                ServicesView(initial: .myProducts)                    .navigationDestination(for: ServicesRoute.self) { route in                        switch route {                        case .myProducts:                            ServicesView(initial: .myProducts)                        case .hireService:                            ServicesView(initial: .hire)                        }                    }                    .navigationTitle("Servicios")                    .navigationBarTitleDisplayMode(.inline)            }            .tabItem { Label("Producto", systemImage: "bag") }            .tag(MainTab.services)            // CALENDAR (sin path porque no empujas nada aún)            NavigationStack {                CalendarView()                    .navigationTitle("Calendario")            }            .tabItem { Label("Calendario", systemImage: "calendar") }            .tag(MainTab.calendar)            // STATS (sin path)            NavigationStack {                StatsView()                    .navigationTitle("Estadísticas")            }            .tabItem { Label("Estadísticas", systemImage: "chart.bar") }            .tag(MainTab.stats)            // USER (sin path)            NavigationStack {                UserView()                    .navigationTitle("Usuario")            }            .tabItem { Label("Usuario", systemImage: "person.circle") }            .tag(MainTab.user)        }        .toolbarBackground(.visible, for: .tabBar)        .toolbarBackground(Color(.systemBackground), for: .tabBar)    }    // Deep-links opcionales    func openServicesMyProducts() {        selected = .services        servicesPath = [.myProducts]   // empuja UNA pantalla    }    func openServicesHire() {        selected = .services        servicesPath = [.hireService]  // empuja UNA pantalla    }}