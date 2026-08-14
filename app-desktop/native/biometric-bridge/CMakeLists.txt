cmake_minimum_required(VERSION 3.25)

project(passvault_biometric VERSION 1.0.0 LANGUAGES C CXX)

include(CTest)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(APPLE)
    enable_language(OBJCXX)
    add_library(passvault_biometric SHARED src/macos/passvault_biometric_macos.mm)
    target_compile_options(passvault_biometric PRIVATE
        -fobjc-arc
        -fvisibility=hidden
        -fstack-protector-strong
        -Wall
        -Wextra
        -Werror
    )
    target_link_options(passvault_biometric PRIVATE
        -Wl,-dead_strip
    )
    target_link_libraries(passvault_biometric PRIVATE
        "-framework Foundation"
        "-framework LocalAuthentication"
        "-framework Security"
    )
    set_target_properties(passvault_biometric PROPERTIES
        OUTPUT_NAME "passvault_biometric"
        INSTALL_NAME_DIR "@loader_path"
        BUILD_WITH_INSTALL_NAME_DIR ON
        MACOSX_RPATH OFF
    )
    if(BUILD_TESTING)
        add_executable(passvault_biometric_macos_security_test
            tests/macos/passvault_biometric_macos_security_test.mm
        )
        target_include_directories(passvault_biometric_macos_security_test PRIVATE include)
        target_compile_definitions(passvault_biometric_macos_security_test PRIVATE
            PASSVAULT_BIOMETRIC_BUILD=1
        )
        target_compile_options(passvault_biometric_macos_security_test PRIVATE
            -fobjc-arc
            -fstack-protector-strong
            -Wall
            -Wextra
            -Werror
        )
        target_link_libraries(passvault_biometric_macos_security_test PRIVATE
            "-framework Foundation"
            "-framework LocalAuthentication"
            "-framework Security"
        )
        add_test(
            NAME passvault_biometric_macos_security
            COMMAND passvault_biometric_macos_security_test
        )
    endif()
elseif(WIN32)
    add_library(passvault_biometric SHARED
        src/windows/passvault_biometric_windows.cpp
        src/windows/passvault_biometric_windows.rc
    )
    target_compile_definitions(passvault_biometric PRIVATE
        NOMINMAX
        UNICODE
        _UNICODE
        WIN32_LEAN_AND_MEAN
        WINVER=0x0A00
        _WIN32_WINNT=0x0A00
    )
    target_compile_options(passvault_biometric PRIVATE
        /W4
        /WX
        /guard:cf
        /sdl
    )
    target_link_options(passvault_biometric PRIVATE
        /DYNAMICBASE
        /HIGHENTROPYVA
        /NXCOMPAT
        /guard:cf
    )
    set_property(TARGET passvault_biometric PROPERTY
        MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>"
    )
    target_link_libraries(passvault_biometric PRIVATE
        advapi32
        bcrypt
        ole32
        runtimeobject
        windowsapp
    )
    set_target_properties(passvault_biometric PROPERTIES OUTPUT_NAME "passvault_biometric")

    if(BUILD_TESTING)
        # Compile the reviewed Windows implementation into a test-only
        # executable as well, so its pure envelope/crypto/validation paths can
        # run on CI without requiring Windows Hello enrollment.
        add_executable(passvault_biometric_windows_security_test
            tests/windows/passvault_biometric_windows_security_test.cpp
        )
        target_include_directories(passvault_biometric_windows_security_test PRIVATE include)
        target_compile_definitions(passvault_biometric_windows_security_test PRIVATE
            NOMINMAX
            UNICODE
            _UNICODE
            WIN32_LEAN_AND_MEAN
            WINVER=0x0A00
            _WIN32_WINNT=0x0A00
            PASSVAULT_BIOMETRIC_BUILD=1
        )
        target_compile_options(passvault_biometric_windows_security_test PRIVATE
            /W4
            /WX
            /guard:cf
            /sdl
        )
        target_link_options(passvault_biometric_windows_security_test PRIVATE
            /DYNAMICBASE
            /HIGHENTROPYVA
            /NXCOMPAT
            /guard:cf
        )
        set_property(TARGET passvault_biometric_windows_security_test PROPERTY
            MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>"
        )
        target_link_libraries(passvault_biometric_windows_security_test PRIVATE
            advapi32
            bcrypt
            ole32
            runtimeobject
            windowsapp
        )
        add_test(
            NAME passvault_biometric_windows_security
            COMMAND passvault_biometric_windows_security_test
        )
    endif()
else()
    message(FATAL_ERROR "The biometric bridge is intentionally unavailable on this platform")
endif()

target_include_directories(passvault_biometric PUBLIC include)
target_compile_definitions(passvault_biometric PRIVATE PASSVAULT_BIOMETRIC_BUILD=1)

if(BUILD_TESTING)
    add_executable(passvault_biometric_abi_test tests/passvault_biometric_abi_test.cpp)
    target_link_libraries(passvault_biometric_abi_test PRIVATE passvault_biometric)
    add_test(NAME passvault_biometric_abi COMMAND passvault_biometric_abi_test)
endif()

install(TARGETS passvault_biometric
    LIBRARY DESTINATION .
    RUNTIME DESTINATION .
)
