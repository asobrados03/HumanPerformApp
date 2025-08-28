import Foundation
import UIKit

/// Genera el contenido de un fichero ICS para un evento.
func createICSFile(eventTitle: String, startDate: Date, durationMinutes: Int = 60) -> String {
    let endDate = Calendar.current.date(byAdding: .minute, value: durationMinutes, to: startDate) ?? startDate
    let formatter = DateFormatter()
    formatter.timeZone = TimeZone.current
    formatter.dateFormat = "yyyyMMdd'T'HHmmss"
    let dtStart = formatter.string(from: startDate)
    let dtEnd = formatter.string(from: endDate)
    return """
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//HumanPerformApp//ES
BEGIN:VEVENT
SUMMARY:\(eventTitle)
DTSTART:\(dtStart)
DTEND:\(dtEnd)
END:VEVENT
END:VCALENDAR
"""
}

/// Comparte el fichero ICS generado usando un `UIActivityViewController`.
func shareICS(content: String, fileName: String = "evento.ics") {
    let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
    try? content.write(to: url, atomically: true, encoding: .utf8)
    let activity = UIActivityViewController(activityItems: [url], applicationActivities: nil)
    if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
       let root = scene.windows.first?.rootViewController {
        root.present(activity, animated: true, completion: nil)
    }
}
