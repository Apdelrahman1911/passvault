import PassVaultShared
import SwiftUI
import UIKit

struct ContentView: View {
    var body: some View {
        ComposeView()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            // Compose owns both safe-area and IME padding. Ignoring the
            // SwiftUI keyboard inset prevents the keyboard height from
            // being applied a second time around the Compose scene.
            .ignoresSafeArea(.container, edges: .all)
            .ignoresSafeArea(.keyboard, edges: .bottom)
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
