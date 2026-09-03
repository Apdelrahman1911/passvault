func unsafeLogging(_ secret: String) {
    // ruleid: passvault.swift.production-logging
    print(secret)
}

let weakProtection =
    // ruleid: passvault.apple.weakened-data-protection
    "NSFileProtectionCompleteUntilFirstUserAuthentication"
