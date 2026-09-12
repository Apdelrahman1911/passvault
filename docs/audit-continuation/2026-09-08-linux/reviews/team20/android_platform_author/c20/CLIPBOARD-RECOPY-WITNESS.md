# C20 source witness — pending clipboard clear must not migrate to a new copy

Author `/root/android_platform_author`, 2026-09-11. **One inert, unexecuted
coverage proposal; no demonstrated product defect or production correction.**

## Scope and exact insertion

`CLIPBOARD-RECOPY-WITNESS.UNAPPLIED.patch.txt` inserts one method before the
existing `withClipboard` helper in
`app-android/src/test/kotlin/com/passvault/android/security/AndroidClipboardServiceTest.kt`.
Required unchanged preimage SHA-256:
`b4a69761ef3f6920ee586f070b5a22a7d552151850bce2718e1a7c06b5c91b64`.
The 40 added lines require no import/dependency/production change, suppression,
existing-method edit, helper replacement or new runner. They are not applied.

Prospective method:
`newSensitiveCopyDoesNotInheritAnUnavailablePendingClearOrPriorExpiry`.

## Realistic missing sequence

The six existing methods cover unreadable expiry, unreadable ownership query,
foreground external-label replacement, readable unlabelled replacement, failed
clear/retry, and foreground before expiry. None makes a new **service-owned**
sensitive copy after `clearRequested` is already pending for an older copy.

The proposed sequence uses the real service and existing fake provider:

1. Copy synthetic A with its own suspended expiry; make provider reads unavailable.
2. Request clear (the service boundary used by lifecycle lock). Assert no provider
   clear and retained ownership; A's expiry has not fired.
3. Copy synthetic B. Require a different nonnull ownership token and exactly two
   registered expiry gates. Restore readable provider access, signal only A's
   gate, then call foreground.
4. Before any ownership query that could retire a wrong owner, require zero
   clear calls and the exact B token/text. B must still be owned.
5. Signal B's independent gate; require exactly one provider clear, empty fake
   text and no sensitive ownership. Another foreground must not clear again.

This is different from an external replacement: B legitimately becomes the new
service-owned clip and must receive its own full timeout. It also differs from
the existing unexpired-copy control because a clear intent already exists for A.

## Oracle and attempted counterexamples (reasoning, not test results)

- The existing helper owns **one** CompletableDeferred. Reusing it after firing
  A would instantly expire B and invalidate the oracle. This proposal deliberately
  creates two gates and checks assignment count; it leaves the helper and six
  historical methods untouched.
- Dispatchers.Unconfined matches the existing deterministic host-test strategy.
  Each timer starts until its own suspended await; signals synchronously resume
  eligible fake-provider work. There is no sleep, device callback or scheduling
  tolerance. A future different dispatcher must not inherit this claim blindly.
- The present production copy already cancels the old timer, replaces the token,
  resets `clearRequested`, and starts a new timer under its mutex. Source reasoning
  predicts the new method passes; **no pass was observed**.
- A hypothetical omitted `clearRequested = false` in the sensitive recopy path
  leaves B incorrectly eligible for foreground clear. The zero-clear/B-tuple
  assertions distinguish that behavior without deriving expected values from
  service internals. This mutation was not made or run.
- Both gates remain distinct. Omitting B's timer cannot pass the registration or
  final clear assertion. Treating the old gate as B's expiry cannot satisfy the
  preservation-before-own-expiry requirement.
- Signalling A after recopy is a cooperative expiry-order observation. The
  cancelled A job need not execute its old callback. This is **not** a forced
  stale-callback test, a proof of a particular cancellation line, true concurrent
  contention or a noncooperative-provider race. The existing token guard may
  independently preserve public correctness even if cancellation is changed.

The method's `finally` cancels its local SupervisorJob scope; it does not claim
framework/process settlement. Data is fixed synthetic text only. Actual Android
ClipboardManager behavior, provider exceptions, device policy, Main focus and
target cleanup are outside this host-fake witness and remain in the accepted
two-case unexecuted framework fixture.

## Disposition and boundaries

Independent review is required before root decides whether this coverage-only
method merits adoption. No extra execution loop or replay of successful suites
is requested; if adopted, root may batch it into a separately admitted affected
host selection. Do not count an inert patch as an adopted or executed test case.

PVA-009 is still implemented/target-runtime-blocked; PVA-030 remains separately
target-blocked. No new family or closure, no source change in production, no
Android32/KDF/ABI result and no target/license authority follows. This proposal
does not change STOP/NO-RETRY/CLOSED/HOLD/native-refusal or build1017001.
