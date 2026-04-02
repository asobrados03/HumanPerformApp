import Foundation

/// Swift-native representation for coach rows in iOS UI.
struct CoachUI: Identifiable, Equatable {
    let id: Int
    let name: String
    let photoName: String?
    let serviceName: String?
}
