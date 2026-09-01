#ifndef PASSVAULT_SODIUM_H
#define PASSVAULT_SODIUM_H

#include <stddef.h>
#include <stdint.h>

int crypto_pwhash(
    unsigned char *out,
    unsigned long long outlen,
    const char *passwd,
    unsigned long long passwdlen,
    const unsigned char *salt,
    unsigned long long opslimit,
    size_t memlimit,
    int alg
);

static inline int passvault_crypto_pwhash(
    unsigned char *out,
    unsigned long long outlen,
    const unsigned char *password,
    unsigned long long password_length,
    const unsigned char *salt,
    unsigned long long opslimit,
    size_t memlimit,
    int algorithm
) {
    return crypto_pwhash(
        out,
        outlen,
        (const char *) password,
        password_length,
        salt,
        opslimit,
        memlimit,
        algorithm
    );
}

#endif
