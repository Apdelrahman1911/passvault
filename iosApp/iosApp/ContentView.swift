import PassVaultShared
import SwiftUI
import UIKit

struct ContentView: View {
    var body: some View {
        ComposeView()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .ignoresSafeArea()
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose owns and updates the view hierarchy.
    }
}
