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
        VStack(spacing: 16) {
            Spacer()

            Text("बाघ चाल")
                .font(.system(size: 36, weight: .bold))
                .foregroundColor(Color(red: 0.1, green: 0.1, blue: 0.11))

            Text("BaghChal")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(Color(red: 0.22, green: 0.53, blue: 0.87))

            Text("Nepal's Traditional Board Game")
                .font(.system(size: 14))
                .foregroundColor(.gray)

            Spacer()

            ProgressView()
                .scaleEffect(1.2)

            Spacer().frame(height: 30)
        }
        .padding(24)
    }
}
