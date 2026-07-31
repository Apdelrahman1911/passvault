#!/bin/bash
# =============================================================================
# PassVault Desktop Build Script
# =============================================================================
# This script builds the desktop application for various platforms.
#
# Usage:
#   ./scripts/build-desktop.sh [options]
#
# Options:
#   --linux           Build Linux packages (.deb, .rpm)
#   --windows         Build Windows packages (.msi, .exe)
#   --macos           Build macOS packages (.dmg)
#   --all             Build all platforms
#   --clean           Clean before building
#   --portable        Create portable distribution
#   --sign            Sign the packages (requires signing config)
#   --help            Show this help message
#
# Environment Variables:
#   MACOS_SIGN                    Enable macOS signing (true/false)
#   MACOS_IDENTITY                macOS signing identity
#   MACOS_NOTARIZATION_APPLE_ID   Apple ID for notarization
#   MACOS_NOTARIZATION_PASSWORD   App-specific password
#   MACOS_NOTARIZATION_TEAM_ID    Apple Team ID
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BUILD_LINUX=false
BUILD_WINDOWS=false
BUILD_MACOS=false
BUILD_ALL=false
CLEAN=false
CREATE_PORTABLE=false
SIGN_PACKAGES=false

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
        --linux)
            BUILD_LINUX=true
            shift
            ;;
        --windows)
            BUILD_WINDOWS=true
            shift
            ;;
        --macos)
            BUILD_MACOS=true
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
        --portable)
            CREATE_PORTABLE=true
            shift
            ;;
        --sign)
            SIGN_PACKAGES=true
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

# If no platform specified and not building all, detect current platform
if [[ "$BUILD_LINUX" == "false" && "$BUILD_WINDOWS" == "false" && "$BUILD_MACOS" == "false" && "$BUILD_ALL" == "false" ]]; then
    case "$(uname -s)" in
        Linux*)
            BUILD_LINUX=true
            print_info "Detected Linux - building Linux packages"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            BUILD_WINDOWS=true
            print_info "Detected Windows - building Windows packages"
            ;;
        Darwin*)
            BUILD_MACOS=true
            print_info "Detected macOS - building macOS packages"
            ;;
        *)
            print_error "Unsupported platform: $(uname -s)"
            exit 1
            ;;
    esac
fi

# If --all is specified, build all platforms
if [[ "$BUILD_ALL" == "true" ]]; then
    BUILD_LINUX=true
    BUILD_WINDOWS=true
    BUILD_MACOS=true
fi

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

print_info "PassVault Desktop Build Script"
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

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2)
print_info "Java version: $JAVA_VERSION"

# Clean if requested
if [[ "$CLEAN" == "true" ]]; then
    print_info "Cleaning project..."
    ./gradlew :app-desktop:clean
    print_success "Clean completed"
fi

# Function to build Linux packages
build_linux() {
    print_info "Building Linux packages..."
    
    # Build DEB package
    print_info "Building DEB package..."
    ./gradlew :app-desktop:packageReleaseDeb --no-daemon
    
    if [[ $? -eq 0 ]]; then
        DEB_PATH=$(find app-desktop/build/compose/binaries -name "*.deb" -type f 2>/dev/null | head -n 1)
        if [[ -n "$DEB_PATH" ]]; then
            print_success "DEB package built successfully!"
            print_info "Package location: ${DEB_PATH}"
            ls -lh "$DEB_PATH"
        fi
    else
        print_error "DEB package build failed!"
        return 1
    fi
    
    # Build RPM package
    print_info "Building RPM package..."
    ./gradlew :app-desktop:packageReleaseRpm --no-daemon
    
    if [[ $? -eq 0 ]]; then
        RPM_PATH=$(find app-desktop/build/compose/binaries -name "*.rpm" -type f 2>/dev/null | head -n 1)
        if [[ -n "$RPM_PATH" ]]; then
            print_success "RPM package built successfully!"
            print_info "Package location: ${RPM_PATH}"
            ls -lh "$RPM_PATH"
        fi
    else
        print_error "RPM package build failed!"
        return 1
    fi
}

# Function to build Windows packages
build_windows() {
    print_info "Building Windows packages..."
    
    # Build MSI package
    print_info "Building MSI package..."
    ./gradlew :app-desktop:packageReleaseMsi --no-daemon
    
    if [[ $? -eq 0 ]]; then
        MSI_PATH=$(find app-desktop/build/compose/binaries -name "*.msi" -type f 2>/dev/null | head -n 1)
        if [[ -n "$MSI_PATH" ]]; then
            print_success "MSI package built successfully!"
            print_info "Package location: ${MSI_PATH}"
            ls -lh "$MSI_PATH" 2>/dev/null || print_info "File: ${MSI_PATH}"
        fi
    else
        print_warning "MSI package build may have issues (this is common on non-Windows)"
    fi
    
    # Build EXE package
    print_info "Building EXE package..."
    ./gradlew :app-desktop:packageReleaseExe --no-daemon
    
    if [[ $? -eq 0 ]]; then
        EXE_PATH=$(find app-desktop/build/compose/binaries -name "*.exe" -type f 2>/dev/null | head -n 1)
        if [[ -n "$EXE_PATH" ]]; then
            print_success "EXE package built successfully!"
            print_info "Package location: ${EXE_PATH}"
            ls -lh "$EXE_PATH" 2>/dev/null || print_info "File: ${EXE_PATH}"
        fi
    else
        print_warning "EXE package build may have issues (this is common on non-Windows)"
    fi
}

# Function to build macOS packages
build_macos() {
    print_info "Building macOS packages..."
    
    # Check if running on macOS
    if [[ "$(uname -s)" != "Darwin" ]]; then
        print_warning "Not running on macOS. macOS builds require macOS environment."
        print_warning "Skipping macOS build."
        return 0
    fi
    
    # Set signing variables if requested
    if [[ "$SIGN_PACKAGES" == "true" ]]; then
        export MACOS_SIGN=true
        if [[ -z "$MACOS_IDENTITY" ]]; then
            print_error "MACOS_IDENTITY not set for signing"
            return 1
        fi
        print_info "Will sign with identity: ${MACOS_IDENTITY}"
    fi
    
    # Build DMG package
    print_info "Building DMG package..."
    ./gradlew :app-desktop:packageReleaseDmg --no-daemon
    
    if [[ $? -eq 0 ]]; then
        DMG_PATH=$(find app-desktop/build/compose/binaries -name "*.dmg" -type f 2>/dev/null | head -n 1)
        if [[ -n "$DMG_PATH" ]]; then
            print_success "DMG package built successfully!"
            print_info "Package location: ${DMG_PATH}"
            ls -lh "$DMG_PATH"
        fi
    else
        print_error "DMG package build failed!"
        return 1
    fi
}

# Function to create portable distribution
create_portable() {
    print_info "Creating portable distribution..."
    
    ./gradlew :app-desktop:packagePortable --no-daemon 2>/dev/null || {
        print_warning "Portable task not found, creating manually..."
        
        # Create runtime image first
        ./gradlew :app-desktop:createRuntimeImage --no-daemon
        
        # Create portable ZIP
        RUNTIME_DIR="app-desktop/build/compose/binaries/main/app"
        if [[ -d "$RUNTIME_DIR" ]]; then
            VERSION=$(grep "^version=" gradle.properties 2>/dev/null | cut -d'=' -f2 || echo "1.0.0")
            PORTABLE_NAME="passvault-${VERSION}-portable"
            
            cd "$RUNTIME_DIR/../"
            if command -v zip &> /dev/null; then
                zip -r "${PORTABLE_NAME}.zip" app/
                print_success "Portable distribution created: ${PORTABLE_NAME}.zip"
            else
                print_warning "zip command not found, cannot create portable distribution"
            fi
            cd "${PROJECT_ROOT}"
        fi
    }
}

# Build platforms
BUILD_FAILED=false

if [[ "$BUILD_LINUX" == "true" ]]; then
    build_linux || BUILD_FAILED=true
fi

if [[ "$BUILD_WINDOWS" == "true" ]]; then
    build_windows || BUILD_FAILED=true
fi

if [[ "$BUILD_MACOS" == "true" ]]; then
    build_macos || BUILD_FAILED=true
fi

# Create portable distribution if requested
if [[ "$CREATE_PORTABLE" == "true" ]]; then
    create_portable || print_warning "Portable distribution creation failed"
fi

# Summary
print_info "================================"
if [[ "$BUILD_FAILED" == "true" ]]; then
    print_error "Build completed with errors!"
    exit 1
else
    print_success "Build completed successfully!"
    print_info "Build outputs:"
    find app-desktop/build/compose/binaries -type f \( -name "*.deb" -o -name "*.rpm" -o -name "*.msi" -o -name "*.exe" -o -name "*.dmg" -o -name "*.zip" \) 2>/dev/null | while read -r file; do
        ls -lh "$file" 2>/dev/null || echo "  $file"
    done
fi
