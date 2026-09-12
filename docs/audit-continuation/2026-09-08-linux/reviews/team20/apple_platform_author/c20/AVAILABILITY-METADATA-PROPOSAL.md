# C20 Apple availability: one bounded root-only metadata check

Author `/root/apple_platform_author`, 2026-09-11, following root's explicit
request for exact metadata endpoints/commands. **INERT; NO COMMAND EXECUTED.**
Independent review of this new proposal is separate from the accepted original
Apple source assessment. Root must bind its current collector/command custody
and independently admit this check; this document supplies no run authority.

## Prior context, no credentials

The retained root read `gh api repos/Apdelrahman1911/passvault/actions/runs/34454557777`
completed with exit0. `B/runs/ios-focused01/RUN.json` SHA256
`5b578327aa528eb3bbc8c117c20629ddfc78eb2dda9506e1c5186cedc24c259b`
identifies public repository **1318415796**, full name
`Apdelrahman1911/passvault`, owner **104788132**, login `Apdelrahman1911`, type
**User**. Its exact root read receipt is `DOWNLOAD-RECEIPT.json` SHA256
`c4075b769ee0dfcfef4274cd57a12919373a7da71fce62f9d3c483d04b0d8505`.
This demonstrates prior root API transport, not current token scope, account
plan, repository admin authority or available larger-runner entitlement.

`JOBS.json` SHA256
`7872f40faae42253d6cf644455ceff4eeea8be2c31be3a956547f93bb48d39a3`
names only `macos-15`, runner1000005399 and default GitHub Actions group0. This
is a terminated historical hosted runner, not an owned host to probe or reuse.

The retained `reviews/android32/runner-facts-01/runner-images-README.md`
(SHA256 `e2282f1ccf4a8c952433ada336e1eeda9eaffe97592c9b9c334fcaac6115f63d`)
at lines34/46 lists `macos-15-xlarge` as ARM64 and links the exact public
larger-runner documentation URL used below. It is **only a documented label**,
not an available allocation or adequate free-memory observation. No larger
runner, organization, runner group or account-plan identity was supplied.

## Fixed allowlist: at most three serial GET commands, once each

Use the existing root-owned authenticated `gh` terminal context for only the
two GitHub API reads. Do not log credentials, enable HTTP/auth debugging, run
`gh auth status/token/login/refresh`, query `/user` or billing, or pass its
authentication to the public documentation request. Root binds the actual
executables; `gh` and `curl` below are command names, not guessed installed paths.
The collector must reject or scrub inherited `GH_DEBUG` and equivalent HTTP/
auth/shell tracing **without printing the environment**. Disable TLS key logging
(`SSLKEYLOGFILE` and equivalent facilities) for every child. Preserve only the
existing necessary GitHub credential context for commands1/2; never expose it in
argv, diagnostics or evidence. Command3 must use a fresh minimal environment
allowlisting only root-bound `PATH`, `LANG`, `LC_ALL` and `TZ`: no GitHub token/
credential/config variables, home/config overrides, tracing, TLS-key-log or proxy
credential variables. It must not receive or inspect the GitHub credential
configuration. No additional discovery/auth command is authorized by these guards.

**1. Current repository identity and existing permission projection:**

```text
gh api --hostname github.com --method GET \
  -H 'Accept: application/vnd.github+json' \
  -H 'X-GitHub-Api-Version: 2022-11-28' \
  'repos/Apdelrahman1911/passvault' \
  --jq '{id,full_name,private,visibility,archived,disabled,owner:{id:.owner.id,login:.owner.login,type:.owner.type},permissions:{admin:.permissions.admin,pull:.permissions.pull}}'
```

Require JSON objects of the projected shape, integer repository/owner IDs equal
to the retained IDs (not booleans/coerced strings), string full name equal to
`Apdelrahman1911/passvault`, string `owner.login == "Apdelrahman1911"`, string
`owner.type == "User"`, string `visibility == "public"`, and exact booleans
`private == false`, `archived == false`, `disabled == false`. Both projected
permission fields must be booleans. Command2 additionally requires
`permissions.admin == true`. A well-formed, otherwise matching response with
`admin == false` skips only command2; command3 may proceed. Missing/wrong-typed
fields, unavailable permission metadata, transfer/identity change, nonzero child
exit, timeout, exceeded bound or ambiguous custody **aborts all remaining
commands**. There is no alternate owner/organization lookup. Such refusal is an
unknown/changed authority condition, not proof no Apple runner exists.

**2. Existing repository-visible self-hosted candidates only:**

```text
gh api --hostname github.com --method GET \
  -H 'Accept: application/vnd.github+json' \
  -H 'X-GitHub-Api-Version: 2022-11-28' \
  'repos/Apdelrahman1911/passvault/actions/runners?per_page=100&page=1' \
  --jq '{total_count,runners:[.runners[]|{id,name,os,status,busy,labels:[.labels[]|{name,type}]}]}'
```

This endpoint lists **self-hosted runners**, not standard hosted or larger
hosted-runner inventory. An online/not-busy macOS runner with an ARM64 label is
at most an accessible *candidate*. Names/labels can be configured; none proves
physical architecture, RAM/free pages/disk, installed/licensed Xcode/JDK/runtime,
exclusive custody, a disposable simulator, or protection/biometric hardware.
The response is not permission to send any job, SSH, SDK or device probe to it.

Require a nonnegative integer total and a runners array of at most100 entries.
For a complete-inventory claim require array length equal to total_count and
unique positive integer runner IDs, never booleans/coerced values. Require each
runner's `busy` to be a boolean and `name`, `os`, `status` to be nonempty,
control-free strings of at most256 characters; require at most100 label objects
per runner, with nonempty control-free `name`/`type` strings of at most256
characters. Unknown OS/status/label values are not affirmative candidates; no
string is interpolated into a new command. Wrong types/shapes abort remaining
commands. If total_count exceeds100 or the list is shorter than the declared
total, record **incomplete** and stop; no pagination, follow-up runner GET,
fallback, polling or retry.403/404/other command errors abort the remainder and
mean unavailable/inadequate query authority, not an empty inventory.
Root keeps candidate host names/labels private unless separately cleared for
public evidence; a public summary can retain counts and exact-response digest.

**3. Public account/runner eligibility documentation, unauthenticated:**

```text
curl -q --silent --show-error --fail --retry 0 \
  --proto '=https' --tlsv1.2 --connect-timeout 10 --max-time 30 \
  --max-redirs 0 --max-filesize 1048576 \
  --header 'Accept: text/html' \
  --write-out '\nPASSVAULT_HTTP_STATUS=%{http_code}\n' \
  'https://docs.github.com/en/actions/reference/runners/larger-runners'
```

The URL is the retained official link minus its browser fragment, not a guessed
API. `-q` must be the first curl option, avoiding an ambient curl config. There
is no `--location`, cookie, auth header, proxy override, install or output file
switch. The status must be parsed from the **final exact suffix**
`\nPASSVAULT_HTTP_STATUS=<three ASCII digits>\n`, never by searching the body;
retain the preceding bytes separately as HTML. The suffix belongs to the
collector/curl format, not the HTML body. Require the complete untruncated
capture, exit0 and final status200; a redirect, timeout, excess output or
missing eligibility statement remains unestablished, without trying another
URL. Read the relevant account eligibility/macOS label text as inert source;
no JavaScript or linked asset is loaded.

This can establish **documented** hosted eligibility at capture time. It cannot
establish a paid entitlement, billing availability, reservation, installed image
or free resources. If documentation requires organization/account features not
established for this User-owned repository, report that exact gap; do not guess
`/orgs/Apdelrahman1911/actions/hosted-runners`, transfer ownership, enroll in a plan,
or select `macos-15-xlarge` just to see whether a job queues. If the page instead
supports a personal-account route, additional concrete account/host evidence
still needs a new root decision; no unreviewed endpoint expansion follows.

## Root capture/admission boundary

At most three serial foreground invocations; **30s each,100s aggregate**;
API projected stdout cap256KiB each, documentation capture cap1MiB plus128B
status trailer, stderr16KiB per child and2MiB aggregate evidence. These are
collector-enforced deadlines/byte caps, not claims that `gh --jq` bounds the
raw HTTP response in memory; pagination is additionally limited as above.
Three CLI invocations are not a guarantee of exactly three HTTP transactions:
`gh`'s internal redirect/auth/retry behavior was not inspected. The collector
may not add application-level retries or expand the endpoint allowlist; curl's
explicit no-retry/no-follow contract is separate from `gh`'s uninspected internals.
Root's independently reviewed metadata collector must enforce them and retain
original argv, start/end, actual exit, owned direct-child reaping, exact projected
stdout bytes/digest and document/status separation. No discarded partial output
may be represented as a complete response. Stop on bound/custody/error ambiguity;
only positively owned metadata children may be settled. No background polls,
source/runtime lock adoption or reuse of consumed collectors is granted.

Root may use its current API capture mechanism after review rather than create
a new helper. No executable collector or cleanup implementation is authored
here. Git access, project/helper imports, SDK discovery, build/test/CI dispatch,
runner registration/removal/label changes, permissions/billing/protection changes,
secrets, physical devices, held-runtime reads and any cleanup are excluded.

## Decision after the check

- A complete empty runner list says no *configured repository self-hosted*
  candidate was listed at that instant. It does not exhaust standard/larger
  hosting or owner-supplied Macs.
- A positive candidate requires owner/root custody and actual compatible
  toolchain/resource facts, then separate exact nonpublishing source/instance
  and execution/cleanup admission. Online/not-busy is not exclusivity.
- If no supported candidate/entitlement is established, preserve **external
  accessible compatible Apple-Silicon host required**, and continue the already
  independently source-accepted inert PVA014 default-publication delta only after
  root's exact source-edit release. Do not spend another cycle scaffolding an
  unlaunchable simulator job.

All IOS01 consumed failure/10UNSTARTED/0XML/unmet close, Mac02 qualified-success,
PVA008/014/PVU004 physical/framework, native-refusal, STOP/NO-RETRY/CLOSED/HOLD,
PVD and protected-ref/dependency/identity/version/signing/Store/1017001 boundaries
remain. Metadata acceptance is not execution, test evidence or a closure.
