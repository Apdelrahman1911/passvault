#!/bin/bash
# =============================================================================
# PassVault Test Script
# =============================================================================
# This script runs all tests for the PassVault project.
#
# Usage:
#   ./scripts/run-tests.sh [options]
#
# Options:
#   --unit              Run unit tests only
#   --android           Run Android instrumented tests only
#   --desktop           Run desktop tests only
#   --coverage          Generate coverage report
#   --all               Run all tests (default)
#   --ci                CI mode (non-interactive, with reports)
#   --clean             Clean before testing
#   --no-daemon         Disable Gradle daemon
#   --parallel          Run tests in parallel
#   --help              Show this help message
#
# Examples:
#   ./scripts/run-tests.sh                    # Run all tests
#   ./scripts/run-tests.sh --unit             # Run unit tests only
#   ./scripts/run-tests.sh --coverage         # Run tests with coverage
#   ./scripts/run-tests.sh --ci               # Run in CI mode
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default values
RUN_UNIT=true
RUN_ANDROID=false
RUN_DESKTOP=false
GENERATE_COVERAGE=false
CI_MODE=false
CLEAN=false
NO_DAEMON=false
PARALLEL=false

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

print_section() {
    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}========================================${NC}\n"
}

# Function to show help
show_help() {
    grep "^#" "$0" | sed 's/^# //g' | sed 's/^#//g'
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --unit)
            RUN_UNIT=true
            RUN_ANDROID=false
            RUN_DESKTOP=false
            shift
            ;;
        --android)
            RUN_UNIT=false
            RUN_ANDROID=true
            shift
            ;;
        --desktop)
            RUN_UNIT=false
            RUN_DESKTOP=true
            shift
            ;;
        --coverage)
            GENERATE_COVERAGE=true
            shift
            ;;
        --all)
            RUN_UNIT=true
            RUN_ANDROID=true
            RUN_DESKTOP=true
            shift
            ;;
        --ci)
            CI_MODE=true
            NO_DAEMON=true
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        --no-daemon)
            NO_DAEMON=true
            shift
            ;;
        --parallel)
            PARALLEL=true
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

# Get script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

print_section "PassVault Test Suite"
print_info "Project root: ${PROJECT_ROOT}"

# Change to project root
cd "${PROJECT_ROOT}"

# Check if gradlew exists
if [[ ! -f "./gradlew" ]]; then
    print_error "gradlew not found. Are you in the project root?"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Build Gradle arguments
GRADLE_ARGS=""
if [[ "$NO_DAEMON" == "true" ]]; then
    GRADLE_ARGS="${GRADLE_ARGS} --no-daemon"
fi
if [[ "$CI_MODE" == "true" ]]; then
    GRADLE_ARGS="${GRADLE_ARGS} --quiet"
fi

# Clean if requested
if [[ "$CLEAN" == "true" ]]; then
    print_info "Cleaning project..."
    ./gradlew clean ${GRADLE_ARGS}
    print_success "Clean completed"
fi

# Track test results
TESTS_FAILED=false

# Function to run unit tests
run_unit_tests() {
    print_section "Running Unit Tests"
    
    local TEST_ARGS="test"
    
    if [[ "$GENERATE_COVERAGE" == "true" ]]; then
        TEST_ARGS="koverHtmlReport koverXmlReport"
        print_info "Coverage reports will be generated"
    fi
    
    if [[ "$PARALLEL" == "true" ]]; then
        TEST_ARGS="${TEST_ARGS} --parallel"
    fi
    
    print_info "Executing: ./gradlew ${TEST_ARGS} ${GRADLE_ARGS}"
    
    if ./gradlew ${TEST_ARGS} ${GRADLE_ARGS}; then
        print_success "Unit tests passed!"
        
        if [[ "$GENERATE_COVERAGE" == "true" ]]; then
            print_info "Coverage report location:"
            find . -path "*/build/reports/kover/*" -name "*.html" -type f 2>/dev/null | head -5
        fi
        
        return 0
    else
        print_error "Unit tests failed!"
        return 1
    fi
}

# Function to run Android instrumented tests
run_android_tests() {
    print_section "Running Android Instrumented Tests"
    
    # Check if Android SDK is available
    if [[ -z "$ANDROID_HOME" && -z "$ANDROID_SDK_ROOT" ]]; then
        print_warning "Android SDK not found. Skipping Android instrumented tests."
        print_warning "Set ANDROID_HOME or ANDROID_SDK_ROOT environment variable."
        return 0
    fi
    
    # Check if connected device or emulator is available
    if ! adb devices | grep -q "device$"; then
        print_warning "No Android device or emulator connected."
        print_warning "Skipping Android instrumented tests."
        return 0
    fi
    
    print_info "Running Android instrumented tests..."
    
    if ./gradlew :app-android:connectedCheck ${GRADLE_ARGS}; then
        print_success "Android instrumented tests passed!"
        return 0
    else
        print_error "Android instrumented tests failed!"
        return 1
    fi
}

# Function to run desktop tests
run_desktop_tests() {
    print_section "Running Desktop Tests"
    
    print_info "Running desktop unit tests..."
    
    if ./gradlew :app-desktop:test ${GRADLE_ARGS}; then
        print_success "Desktop tests passed!"
        return 0
    else
        print_error "Desktop tests failed!"
        return 1
    fi
}

# Function to generate test report
generate_test_report() {
    print_section "Test Report Summary"
    
    print_info "Test result locations:"
    
    # Find test result XML files
    local RESULT_FILES=$(find . -path "*/build/test-results/*" -name "*.xml" -type f 2>/dev/null)
    
    if [[ -n "$RESULT_FILES" ]]; then
        local TOTAL_TESTS=0
        local PASSED_TESTS=0
        local FAILED_TESTS=0
        local SKIPPED_TESTS=0
        
        for file in $RESULT_FILES; do
            # Parse XML for test counts
            local FILE_TOTAL=$(grep -oP 'tests="\K[0-9]+' "$file" 2>/dev/null | head -1 || echo "0")
            local FILE_FAILURES=$(grep -oP 'failures="\K[0-9]+' "$file" 2>/dev/null | head -1 || echo "0")
            local FILE_ERRORS=$(grep -oP 'errors="\K[0-9]+' "$file" 2>/dev/null | head -1 || echo "0")
            local FILE_SKIPPED=$(grep -oP 'skipped="\K[0-9]+' "$file" 2>/dev/null | head -1 || echo "0")
            
            TOTAL_TESTS=$((TOTAL_TESTS + FILE_TOTAL))
            FAILED_TESTS=$((FAILED_TESTS + FILE_FAILURES + FILE_ERRORS))
            SKIPPED_TESTS=$((SKIPPED_TESTS + FILE_SKIPPED))
        done
        
        PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS - SKIPPED_TESTS))
        
        echo ""
        echo -e "${CYAN}Test Summary:${NC}"
        echo "  Total tests:  ${TOTAL_TESTS}"
        echo -e "  ${GREEN}Passed:${NC}     ${PASSED_TESTS}"
        echo -e "  ${RED}Failed:${NC}     ${FAILED_TESTS}"
        echo -e "  ${YELLOW}Skipped:${NC}    ${SKIPPED_TESTS}"
        echo ""
        
        if [[ $FAILED_TESTS -gt 0 ]]; then
            print_error "Some tests failed!"
            return 1
        else
            print_success "All tests passed!"
            return 0
        fi
    else
        print_warning "No test result files found"
        return 0
    fi
}

# Function to open coverage report
open_coverage_report() {
    if [[ "$GENERATE_COVERAGE" == "true" && "$CI_MODE" == "false" ]]; then
        local COVERAGE_HTML=$(find . -path "*/build/reports/kover/html/index.html" -type f 2>/dev/null | head -1)
        if [[ -n "$COVERAGE_HTML" ]]; then
            print_info "Opening coverage report..."
            if command -v xdg-open &> /dev/null; then
                xdg-open "$COVERAGE_HTML" &
            elif command -v open &> /dev/null; then
                open "$COVERAGE_HTML"
            fi
        fi
    fi
}

# Run tests based on arguments
if [[ "$RUN_UNIT" == "true" ]]; then
    run_unit_tests || TESTS_FAILED=true
fi

if [[ "$RUN_ANDROID" == "true" ]]; then
    run_android_tests || TESTS_FAILED=true
fi

if [[ "$RUN_DESKTOP" == "true" ]]; then
    run_desktop_tests || TESTS_FAILED=true
fi

# Generate summary report
if [[ "$CI_MODE" == "true" || "$RUN_UNIT" == "true" ]]; then
    generate_test_report || TESTS_FAILED=true
fi

# Final summary
print_section "Test Execution Complete"

if [[ "$TESTS_FAILED" == "true" ]]; then
    print_error "Some tests failed!"
    print_info "Check test reports for details:"
    find . -path "*/build/reports/tests/*" -name "index.html" -type f 2>/dev/null | while read -r file; do
        echo "  - $file"
    done
    exit 1
else
    print_success "All tests passed successfully!"
    open_coverage_report
    exit 0
fi
