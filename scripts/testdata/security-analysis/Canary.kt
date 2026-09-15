import javax.crypto.Cipher

fun passVaultSecurityAnalysisCanary(): Cipher =
    Cipher.getInstance("AES/ECB/PKCS5Padding")
