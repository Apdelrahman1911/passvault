package com.passvault.core.security

/**
 * Interface for desktop keyring/keychain integration.
 * Stores the vault key securely using the OS keychain.
 */
interface KeyringService {
    /**
     * Store a secret in the keyring.
     * 
     * @param service The service identifier
     * @param account The account identifier
     * @param secret The secret to store
     * @return Result indicating success or failure
     */
    suspend fun storeSecret(
        service: String,
        account: String,
        secret: ByteArray,
    ): Result<Unit>
    
    /**
     * Retrieve a secret from the keyring.
     * 
     * @param service The service identifier
     * @param account The account identifier
     * @return Result containing the secret or null if not found
     */
    suspend fun retrieveSecret(
        service: String,
        account: String,
    ): Result<ByteArray?>
    
    /**
     * Delete a secret from the keyring.
     * 
     * @param service The service identifier
     * @param account The account identifier
     * @return Result indicating success or failure
     */
    suspend fun deleteSecret(
        service: String,
        account: String,
    ): Result<Unit>
    
    /**
     * Check if a secret exists in the keyring.
     * 
     * @param service The service identifier
     * @param account The account identifier
     * @return Result containing true if the secret exists
     */
    suspend fun hasSecret(
        service: String,
        account: String,
    ): Result<Boolean>
    
    /**
     * List all accounts for a service.
     * 
     * @param service The service identifier
     * @return Result containing the list of account names
     */
    suspend fun listAccounts(
        service: String,
    ): Result<List<String>>
}
