import SwiftUI
import WebKit

@main
struct BaghChalApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct GameWebView: UIViewRepresentable {
    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.preferences.setValue(true, forKey: "allowFileAccessFromFileURLs")

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.scrollView.isScrollEnabled = false
        webView.isOpaque = false
        webView.backgroundColor = UIColor(red: 248/255.0, green: 249/255.0, blue: 250/255.0, alpha: 1.0)

        if let htmlPath = Bundle.main.path(forResource: "bagh_chal_bilingual", ofType: "html") {
            let url = URL(fileURLWithPath: htmlPath)
            webView.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent())
        }
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        GameWebView()
            .ignoresSafeArea()
    }
}
