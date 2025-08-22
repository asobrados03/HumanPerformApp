import SwiftUI
import shared

struct ContentView: View {
    private let bridge = PlatformBridge()
    
    var body: some View {
        Text("Human Perform iOS (\(bridge.platformName()))")
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
