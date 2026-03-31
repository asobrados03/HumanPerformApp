import SwiftUI

enum ServicesSection: Int, CaseIterable, Identifiable {
    case myProducts = 0, hire
    var id: Int { rawValue }
    var title: String { self == .myProducts ? "Mis productos" : "Contratar" }
}

struct ServicesView: View {
    @SceneStorage("services.selected") private var selectedRaw = ServicesSection.myProducts.rawValue
    private let onOpenHireProducts: (Int) -> Void
    private let onOpenProductDetail: (Int) -> Void

    private var selected: Binding<ServicesSection> {
        Binding(
            get: { ServicesSection(rawValue: selectedRaw) ?? .myProducts },
            set: { selectedRaw = $0.rawValue }
        )
    }

    init(
        initial: ServicesSection = .myProducts,
        onOpenHireProducts: @escaping (Int) -> Void = { _ in },
        onOpenProductDetail: @escaping (Int) -> Void = { _ in }
    ) {
        _selectedRaw = SceneStorage(wrappedValue: initial.rawValue, "services.selected")
        self.onOpenHireProducts = onOpenHireProducts
        self.onOpenProductDetail = onOpenProductDetail
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

            if UITestConfig.isMockNetworkEnabled {
                Text("Mock Services Loaded")
                    .accessibilityIdentifier("servicesLoadedMarker")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal)
            }

            switch selected.wrappedValue {
            case .myProducts:
                MyProductsView(onOpenProductDetail: onOpenProductDetail)
            case .hire:
                HireServicesView(onOpenHireProducts: onOpenHireProducts)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .principal) { NavBarLogo() } }
        .accessibilityIdentifier("servicesView")
    }
}
