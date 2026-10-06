import SwiftUI

@main
struct BaghChalApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    var body: some View {
        VStack {
            Text("BaghChal iOS")
                .font(.largeTitle)
                .bold()
                .padding()
            Text("Nepal's Traditional Board Game")
                .font(.subheadline)
                .foregroundColor(.gray)
        }
    }
}
