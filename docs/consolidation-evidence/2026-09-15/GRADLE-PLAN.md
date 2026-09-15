# Proposed single focused integration batch

Not yet admitted or executed. No APK/AAB/archive/package, signing, Store or native
provider/GUI operations. Root only. New source tree differs from previous evidence;
this proves regression behavior on the actual selective integration.

- Source: fresh `source/` manifest to be sealed after review, not retired Git/store.
- Java: `/usr/lib/jvm/java-17-openjdk-amd64`, checked-in Gradle9.7.1 wrapper.
- Four tasks in one invocation, one worker/fork; in-process Kotlin; daemon,
  parallelism, configure-on-demand, build/configuration caches disabled; strict
  dependency verification unchanged. No old init script or validation runner.
- Domain: NumericPasswordSequenceTest.
- Database: changed ordinary Bootstrap, batching, ownership, attachment/backup,
  biometric freshness/failure, Unicode pagination, migration tests. Exclude the
  four opt-in cold-provider methods: historical accepted proof reviewed separately.
- Credential: new two-case ownership regression only.
- Desktop: DesktopBiometricKeyStoreTest and baseline JnaDesktopBiometricBridgeTest.
  Real Compose loop test remains permanent and historical-affinity reviewed; no
  rerun without fresh isolated display admission.
- Follow affected Detekt tasks serially in the same process if feasible.
- Explicit fresh HOME/TMP/Gradle cache; existing standard dependency cache may be
  read only via GRADLE_RO_DEP_CACHE, never cleaned. SDK17 and existing SDK read only;
  no SDK install/license acceptance. No real vault or clipboard.
- Resource proposal: normal12GiB cannot currently be recovered safely; owner
  authorized fallback3GiB. Use more conservative8GiB launch/5GiB cancellation guard,
  leaving3GiB minimum headroom. Requires independent current admission; no
  implicit bypass of past held scopes. Max45minutes build,60s original wrapper stop.
- Install signal/exit cleanup before startup; original wrapper `--stop` with same
  environment after invocation. Preserve exact logs/XML/source hashes outside
  cleanup targets. Remove only this run's validated generated module `build/`,
  `.gradle/`, `.kotlin/` outputs and fresh run-specific HOME/TMP/cache after owned
  worker settlement. Stop failure/uncertain children means HOLD, not retries or
  unrelated killing. Shared caches/toolchains/evidence/source remain untouched.
