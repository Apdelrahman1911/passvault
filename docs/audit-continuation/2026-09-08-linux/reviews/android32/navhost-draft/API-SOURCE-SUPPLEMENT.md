# NavHost draft: API source supplement

2026-09-10, `/root/android32`. Source-only; frozen draft/C13 unchanged.

Exact published Compose `ui-desktop:1.11.1` source data is retained in
`compose-api-1.11.1/`. Receipt (source member hashes/license headers/HTTP/cleanup):
`29c36f2f2556a65c73e72a3b36536262075947d42e2eecce91b88ececc708514`.
The sole admitted GET succeeded (200, 1,032,410 bytes); its fresh archive was
removed. No remaining fetch allowance, classloading, compilation or execution.

* `Window` delegates to `SwingWindow`. `ComposeWindow` delegates content/dispose
  to its panel, whose genuine `ComposeContainer` creates
  `DefaultArchitectureComponentsOwner` (container130-143).
* The default owner implements lifecycle/store ownership; container488-495
  makes attached, nonminimized, focused windows RESUMED. Disposal sets DESTROYED;
  owner93-97 then clears its store. No manually resumed test owner is needed
  for that source contract.
* Skiko composition-locals63-116 installs the real lifecycle owner and internal
  VM-store owner. `DefaultViewModelOwnerStore` KDoc25-26 recommends
  `LocalViewModelStoreOwner`, but does not prove that external accessor.

HOLD: retained files omit `SwingWindow` construction and scene-mediator/provider
call links. Relevant cache searches found no source JAR to fill these links or
the external accessor. This is partial, not complete Window-to-local proof.

`MODULE-API-EVIDENCE.json` SHA-256:
`86c37a3d2a6b788bc92a3cc11aa6297a215779b24d66a1c5d5a524a071017933`.
Published UI API metadata exports lifecycle-runtime-compose (minimum2.9.6),
not viewmodel-compose directly. Catalog2.11.0 and shared implementation edges
do not establish app-desktop compile visibility. Root's fresh resolved
classpaths remain required. These are software gaps, not external blockers;
published sources do not independently prove binary correspondence or runtime.
