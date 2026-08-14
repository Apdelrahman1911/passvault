#include "passvault_biometric.h"

int main() { return pv_bio_abi_version() == PV_BIO_ABI_VERSION ? 0 : 1; }
