void unsafe_copy(char* destination, const char* source) {
    // ruleid: passvault.native.unsafe-library-call
    strcpy(destination, source);
}
