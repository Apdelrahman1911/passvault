import PassVaultShared
import SwiftUI
import UIKit

struct ContentView: View {
    let onControllerReady: () -> Void
    let onControllerDisposed: () -> Void

    var body: some View {
        ComposeView(
            onControllerReady: onControllerReady,
            onControllerDisposed: onControllerDisposed
        )
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
    let onControllerDisposed: () -> Void

    final class Coordinator {
        var onControllerDisposed: () -> Void
        private var didDispose = false

        init(onControllerDisposed: @escaping () -> Void) {
            self.onControllerDisposed = onControllerDisposed
        }

        func controllerDidDispose() {
            guard !didDispose else { return }
            didDispose = true
            onControllerDisposed()
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(onControllerDisposed: onControllerDisposed)
    }

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
        context.coordinator.onControllerDisposed = onControllerDisposed
    }

    static func dismantleUIViewController(
        _ uiViewController: UIViewController,
        coordinator: Coordinator
    ) {
        coordinator.controllerDidDispose()
    }
}
