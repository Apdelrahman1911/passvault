#!/bin/bash
# =============================================================================
# PassVault Android Build Script
# =============================================================================
# This script builds the Android APK and AAB files for PassVault.
#
# Usage:
#   ./scripts/build-android.sh [options]
#
# Options:
#   --debug           Build debug variant (default)
#   --release         Build release variant
#   --fdroid          Build F-Droid variant
#   --google          Build Google Play variant
#   --all             Build all variants
#   --clean           Clean before building
#   --test            Run tests after build
#   --help            Show this help message
#
# Environment Variables:
#   KEYSTORE_PATH     Path to signing keystore (for release)
#   KEYSTORE_PASSWORD Keystore password
#   KEY_ALIAS         Key alias
#   KEY_PASSWORD      Key password
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BUILD_DEBUG=false
BUILD_RELEASE=false
BUILD_FDROID=false
BUILD_GOOGLE=false
BUILD_ALL=false
CLEAN=false
RUN_TESTS=false

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show help
show_help() {
    grep "^#" "$0" | sed 's/^# //g' | sed 's/^#//g'
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --debug)
            BUILD_DEBUG=true
            shift
            ;;
        --release)
            BUILD_RELEASE=true
            shift
            ;;
        --fdroid)
            BUILD_FDROID=true
            shift
            ;;
        --google)
            BUILD_GOOGLE=true
            shift
            ;;
        --all)
            BUILD_ALL=true
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        --test)
            RUN_TESTS=true
            shift
            ;;
        --help|-h)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# If no build variant specified, default to debug
if [[ "$BUILD_DEBUG" == "false" && "$BUILD_RELEASE" == "false" && "$BUILD_FDROID" == "false" && "$BUILD_GOOGLE" == "false" && "$BUILD_ALL" == "false" ]]; then
    BUILD_DEBUG=true
fi

# If --all is specified, build everything
if [[ "$BUILD_ALL" == "true" ]]; then
    BUILD_DEBUG=true
    BUILD_RELEASE=true
    BUILD_FDROID=true
    BUILD_GOOGLE=true
fi

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

print_info "PassVault Android Build Script"
print_info "Project root: ${PROJECT_ROOT}"
print_info "================================"

# Change to project root
cd "${PROJECT_ROOT}"

# Check if gradlew exists
if [[ ! -f "./gradlew" ]]; then
    print_error "gradlew not found. Are you in the project root?"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Clean if requested
if [[ "$CLEAN" == "true" ]]; then
    print_info "Cleaning project..."
    ./gradlew clean
    print_success "Clean completed"
fi

# Function to build debug variant
build_debug() {
    print_info "Building Debug APK..."
    ./gradlew :app-android:assembleDebug --no-daemon
    
    # Check if build succeeded
    if [[ $? -eq 0 ]]; then
        APK_PATH="app-android/build/outputs/apk/debug/app-android-debug.apk"
        if [[ -f "$APK_PATH" ]]; then
            print_success "Debug APK built successfully!"
            print_info "APK location: ${APK_PATH}"
            ls -lh "$APK_PATH"
        else
            print_warning "APK not found at expected location"
        fi
    else
        print_error "Debug build failed!"
        return 1
    fi
}

# Function to build release variant
build_release() {
    print_info "Building Release APK..."
    
    # Check for signing configuration
    if [[ -z "$KEYSTORE_PATH" ]]; then
        print_warning "KEYSTORE_PATH not set. Building unsigned release APK..."
        print_warning "To sign the release build, set the following environment variables:"
        print_warning "  - KEYSTORE_PATH"
        print_warning "  - KEYSTORE_PASSWORD"
        print_warning "  - KEY_ALIAS"
        print_warning "  - KEY_PASSWORD"
    fi
    
    ./gradlew :app-android:assembleStandardRelease --no-daemon
    
    if [[ $? -eq 0 ]]; then
        APK_PATH="app-android/build/outputs/apk/standard/release/app-android-standard-release.apk"
        if [[ -f "$APK_PATH" ]]; then
            print_success "Release APK built successfully!"
            print_info "APK location: ${APK_PATH}"
            ls -lh "$APK_PATH"
        else
            print_warning "APK not found at expected location"
        fi
        
        # Also build AAB
        print_info "Building Release AAB..."
        ./gradlew :app-android:bundleStandardRelease --no-daemon
        
        if [[ $? -eq 0 ]]; then
            AAB_PATH="app-android/build/outputs/bundle/standardRelease/app-android-standard-release.aab"
            if [[ -f "$AAB_PATH" ]]; then
                print_success "Release AAB built successfully!"
                print_info "AAB location: ${AAB_PATH}"
                ls -lh "$AAB_PATH"
            fi
        fi
    else
        print_error "Release build failed!"
        return 1
    fi
}

# Function to build F-Droid variant
build_fdroid() {
    print_info "Building F-Droid APK..."
    ./gradlew :app-android:assembleFdroidRelease --no-daemon
    
    if [[ $? -eq 0 ]]; then
        APK_PATH="app-android/build/outputs/apk/fdroid/release/app-android-fdroid-release.apk"
        if [[ -f "$APK_PATH" ]]; then
            print_success "F-Droid APK built successfully!"
            print_info "APK location: ${APK_PATH}"
            ls -lh "$APK_PATH"
        else
            print_warning "APK not found at expected location"
        fi
    else
        print_error "F-Droid build failed!"
        return 1
    fi
}

# Function to build the canonical Google Play artifact
build_google() {
    print_info "Building the Standard variant for Google Play..."
    ./gradlew :app-android:assembleStandardRelease --no-daemon
    
    if [[ $? -eq 0 ]]; then
        APK_PATH="app-android/build/outputs/apk/standard/release/app-android-standard-release.apk"
        if [[ -f "$APK_PATH" ]]; then
            print_success "Google Play APK built successfully!"
            print_info "APK location: ${APK_PATH}"
            ls -lh "$APK_PATH"
        else
            print_warning "APK not found at expected location"
        fi
        
        # Also build AAB
        print_info "Building Google Play AAB..."
        ./gradlew :app-android:bundleStandardRelease --no-daemon
        
        if [[ $? -eq 0 ]]; then
            AAB_PATH="app-android/build/outputs/bundle/standardRelease/app-android-standard-release.aab"
            if [[ -f "$AAB_PATH" ]]; then
                print_success "Google Play AAB built successfully!"
                print_info "AAB location: ${AAB_PATH}"
                ls -lh "$AAB_PATH"
            fi
        fi
    else
        print_error "Google Play build failed!"
        return 1
    fi
}

# Function to run tests
run_tests() {
    print_info "Running tests..."
    ./gradlew test --no-daemon
    
    if [[ $? -eq 0 ]]; then
        print_success "All tests passed!"
    else
        print_error "Some tests failed!"
        return 1
    fi
}

# Build variants
BUILD_FAILED=false

if [[ "$BUILD_DEBUG" == "true" ]]; then
    build_debug || BUILD_FAILED=true
fi

if [[ "$BUILD_RELEASE" == "true" ]]; then
    build_release || BUILD_FAILED=true
fi

if [[ "$BUILD_FDROID" == "true" ]]; then
    build_fdroid || BUILD_FAILED=true
fi

if [[ "$BUILD_GOOGLE" == "true" ]]; then
    build_google || BUILD_FAILED=true
fi

# Run tests if requested
if [[ "$RUN_TESTS" == "true" ]]; then
    run_tests || BUILD_FAILED=true
fi

# Summary
print_info "================================"
if [[ "$BUILD_FAILED" == "true" ]]; then
    print_error "Build completed with errors!"
    exit 1
else
    print_success "Build completed successfully!"
    print_info "Build outputs:"
    find app-android/build/outputs -type f \( -name "*.apk" -o -name "*.aab" \) -exec ls -lh {} \;
fi
