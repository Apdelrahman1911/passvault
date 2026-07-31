#!/bin/bash

# PassVault Release Verification Script
# This script verifies that the app is ready for release

set -e

echo "=== PassVault Release Verification ==="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED=0

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
    else
        echo -e "${RED}✗${NC} $2"
        FAILED=1
    fi
}

# Check if running from project root
if [ ! -f "settings.gradle.kts" ]; then
    echo -e "${RED}Error: Must run from project root${NC}"
    exit 1
fi

echo "1. Checking version configuration..."
if [ -f "version.properties" ]; then
    VERSION=$(grep "VERSION_NAME=" version.properties | cut -d'=' -f2)
    print_status 0 "Version: $VERSION"
else
    print_status 1 "version.properties not found"
fi

echo ""
echo "2. Checking documentation..."

if [ -f "README.md" ]; then
    print_status 0 "README.md exists"
else
    print_status 1 "README.md missing"
fi

if [ -f "CHANGELOG.md" ]; then
    # Check if current version is in changelog
    if grep -q "$VERSION" CHANGELOG.md; then
        print_status 0 "CHANGELOG.md updated for version $VERSION"
    else
        print_status 1 "CHANGELOG.md not updated for version $VERSION"
    fi
else
    print_status 1 "CHANGELOG.md missing"
fi

if [ -f "SECURITY.md" ]; then
    print_status 0 "SECURITY.md exists"
else
    print_status 1 "SECURITY.md missing"
fi

echo ""
echo "3. Checking code quality configuration..."

if [ -f "detekt.yml" ]; then
    print_status 0 "detekt.yml exists"
else
    print_status 1 "detekt.yml missing"
fi

if [ -f ".editorconfig" ]; then
    print_status 0 ".editorconfig exists"
else
    print_status 1 ".editorconfig missing"
fi

echo ""
echo "4. Running code quality checks..."

# Run Detekt
if ./gradlew detekt --quiet > /dev/null 2>&1; then
    print_status 0 "Detekt passed"
else
    print_status 1 "Detekt failed"
fi

echo ""
echo "5. Running tests..."

# Run unit tests
if ./gradlew test --quiet > /dev/null 2>&1; then
    print_status 0 "Unit tests passed"
else
    print_status 1 "Unit tests failed"
fi

echo ""
echo "6. Checking build configurations..."

# Check Android build
if [ -f "app-android/build.gradle.kts" ]; then
    if grep -q "signingConfigs" app-android/build.gradle.kts; then
        print_status 0 "Android signing configuration present"
    else
        print_status 1 "Android signing configuration missing"
    fi
else
    print_status 1 "app-android/build.gradle.kts not found"
fi

# Check Desktop build
if [ -f "app-desktop/build.gradle.kts" ]; then
    if grep -q "nativeDistributions" app-desktop/build.gradle.kts; then
        print_status 0 "Desktop packaging configuration present"
    else
        print_status 1 "Desktop packaging configuration missing"
    fi
else
    print_status 1 "app-desktop/build.gradle.kts not found"
fi

echo ""
echo "7. Checking security configuration..."

# Check ProGuard rules
if [ -f "app-android/proguard-rules.pro" ]; then
    if grep -q "CryptoEngine" app-android/proguard-rules.pro; then
        print_status 0 "ProGuard rules include crypto classes"
    else
        print_status 1 "ProGuard rules missing crypto classes"
    fi
else
    print_status 1 "proguard-rules.pro not found"
fi

# Check for hardcoded secrets
if grep -r "password.*=.*\"" --include="*.kt" --include="*.kts" . > /dev/null 2>&1; then
    print_status 1 "Potential hardcoded secrets found"
else
    print_status 0 "No obvious hardcoded secrets found"
fi

echo ""
echo "8. Checking baseline profile..."

if [ -f "app-android/src/main/baseline-prof.txt" ]; then
    LINE_COUNT=$(wc -l < app-android/src/main/baseline-prof.txt)
    if [ $LINE_COUNT -gt 10 ]; then
        print_status 0 "Baseline profile present ($LINE_COUNT lines)"
    else
        print_status 1 "Baseline profile seems incomplete"
    fi
else
    print_status 1 "baseline-prof.txt not found"
fi

echo ""
echo "9. Building release artifacts..."

# Build Android release
if ./gradlew :app-android:assembleRelease --quiet > /dev/null 2>&1; then
    print_status 0 "Android release build succeeded"
    
    # Check APK size
    APK_SIZE=$(find app-android/build/outputs/apk/release -name "*.apk" -exec stat -f%z {} \; 2>/dev/null || echo "0")
    if [ "$APK_SIZE" != "0" ]; then
        APK_SIZE_MB=$(echo "scale=2; $APK_SIZE / 1024 / 1024" | bc)
        echo "   APK size: ${APK_SIZE_MB}MB"
        
        if [ $(echo "$APK_SIZE_MB > 50" | bc) -eq 1 ]; then
            print_status 1 "APK size exceeds 50MB ($APK_SIZE_MB)"
        else
            print_status 0 "APK size is acceptable ($APK_SIZE_MB MB)"
        fi
    fi
else
    print_status 1 "Android release build failed"
fi

# Build Desktop release
if ./gradlew :app-desktop:packageReleaseDistributionForCurrentOS --quiet > /dev/null 2>&1; then
    print_status 0 "Desktop release build succeeded"
else
    print_status 1 "Desktop release build failed"
fi

echo ""
echo "10. Final summary..."

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}=== All checks passed! Ready for release. ===${NC}"
    exit 0
else
    echo -e "${RED}=== Some checks failed. Please fix before releasing. ===${NC}"
    exit 1
fi
