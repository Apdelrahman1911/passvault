# C20 fixed KDF oracle: inert calculation proposal

Author `/root/native_security_author`; root requested this narrow preparation on
2026-09-11. Independent oracle challenger: `/root/android_fixture_review`.
**NOT RUN. No expected bytes have been calculated by this author.** This is one
inert source block, not a generator executable, platform harness or source patch.

Proposed root-only invocation: existing admitted Python with argv
`python3 -I -B -S -`, the exact 34-line block below as stdin, no file output or
cache. Root separately binds the exact invocation after independent acceptance.
Only Python standard-library hashlib/hmac/json are imported. Inputs are literal
public synthetic bytes; there are no source/helper reads, environment/process
probes, application-native/CNG/credential API calls, randomness, network,
subprocesses or writes.

```python
import hashlib
import hmac
import json

domain = b"PassVault Windows Hello vault-key wrapping v1"
prf = bytes(range(0x47, 0x67))
salt = bytes(range(0x33, 0x53))
vault = bytes(range(0x01, 0x21))
assert (len(domain), len(prf), len(salt), len(vault)) == (45, 32, 32, 32)

def explicit_hmac(key, message):
    assert len(key) == 32
    block = key + bytes(32)
    inner_pad = bytes(value ^ 0x36 for value in block)
    outer_pad = bytes(value ^ 0x5c for value in block)
    return hashlib.sha256(outer_pad + hashlib.sha256(inner_pad + message).digest()).digest()

records = []
for name, vault_hash in (("ascending", vault), ("vault_first_byte_zero", b"\x00" + vault[1:])):
    info = domain + vault_hash + b"\x01"
    assert len(vault_hash) == 32 and len(info) == 78
    prk = hmac.new(salt, prf, hashlib.sha256).digest()
    output = hmac.new(prk, info, hashlib.sha256).digest()
    reference_prk = explicit_hmac(salt, prf)
    reference_output = explicit_hmac(reference_prk, info)
    assert len(prk) == len(output) == 32
    assert prk == reference_prk and output == reference_output
    records.append({"name": name, "vault_hash_hex": vault_hash.hex(),
                    "info_hex": info.hex(), "output_hex": output.hex()})
assert records[0]["output_hex"] != records[1]["output_hex"]
print(json.dumps({"schema": 1, "algorithm": "HMAC-SHA256 extract then one-block expand",
                  "domain_hex": domain.hex(), "prf_hex": prf.hex(), "salt_hex": salt.hex(),
                  "output_bytes": 32, "vectors": records},
                 sort_keys=True, separators=(",", ":")))
```

Math: PRK = HMAC-SHA256(key=salt32, message=PRF32); output32 =
HMAC-SHA256(key=PRK32, message=literal-domain45 || raw-vault-hash32 || byte01).
There is no password, PBKDF iteration count, variable output expansion or claimed
cross-platform counterpart. The counter is binary01, not ASCII `1`; the domain
excludes a NUL. The two inputs differ only at vault-hash byte0. The explicit-pad
cross-check uses HMAC's64-byte SHA256 block and known32-byte keys, so no long-key
normalization branch is needed. Both calculations share hashlib's SHA256;
agreement is not two independent cryptographic implementations or provider proof.
Neither calculation calls or parses the implementation under test.

Source relationship (absolute paths; bytes are unchanged from Windows05):

- `/root/projects/PassVault/passvault-linux/app-desktop/native/biometric-bridge/src/windows/passvault_biometric_windows.cpp`,
  SHA256 `a7c0c97a3b09156afe463c2b1c825bae8330ac07488e9f2667baa2dcab27f215`:
  domain51; HMAC230-269; derive272-296. Production remains unchanged.
- `/root/projects/PassVault/passvault-linux/app-desktop/native/biometric-bridge/tests/windows/passvault_biometric_windows_security_test.cpp`,
  SHA256 `ed5f23e50ca24d01f12c99d2800fab06ad93d42e004656676b87400ad49e588a`:
  input pattern742-761; circular roundtrip763-782; dispatch620-642. Future
  normal-only KAT needs an early argc2 selector before random/temp/context setup.
- `/root/projects/PassVault/passvault-linux/app-desktop/native/biometric-bridge/CMakeLists.txt`,
  SHA256 `976be93860deacf21db1542f26c5a4b2fbcc09d84cabba6990a0518666e409d8`:
  existing security target107-114, shared hardening155-188, registration189-192.
  Future KAT adds one registration to that same target, not a new executable.

If independently accepted and root invokes once, retain exact stdin/argv,
exit/stdout/stderr and this proposal identity as compact public-synthetic oracle
provenance. Failure/disagreement is not a vector or automatic retry authority.
The resulting two full32-byte literals still require independent byte/patch
review before test-source promotion. No runtime count, family/PVU closure,
native-refusal reinterpretation, old-run replay or cleanup authority follows.
