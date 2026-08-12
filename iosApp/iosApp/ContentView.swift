import PassVaultShared
import SwiftUI
import UIKit

struct ContentView: View {
    let onControllerReady: () -> Void

    var body: some View {
        ComposeView(onControllerReady: onControllerReady)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            // Compose owns both safe-area and IME padding. Ignoring the
            // SwiftUI keyboard inset prevents the keyboard height from
            // being applied a second time around the Compose scene.
            .ignoresSafeArea(.container, edges: .all)
            .ignoresSafeArea(.keyboard, edges: .bottom)
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    let onControllerReady: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewControllerKt.mainViewController()
        // mainViewController() starts Koin before it returns. Re-drive native
        // readiness explicitly because SwiftUI's initial scenePhase callback
        // may have run while the dependency graph was still unavailable.
        onControllerReady()
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose owns and updates the view hierarchy.
    }
}
