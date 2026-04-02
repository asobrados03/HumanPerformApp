import Foundation
import shared

extension Professional {
    func toCoachUI() -> CoachUI {
        CoachUI(
            id: Int(id),
            name: name,
            photoName: photoName,
            serviceName: service
        )
    }
}

extension Array where Element == Professional {
    func toCoachUIs() -> [CoachUI] {
        map { $0.toCoachUI() }
    }
}
