import java.util.Random
import javax.crypto.Cipher
import javax.net.ssl.HostnameVerifier

fun insecureCipher(): Cipher =
    // ruleid: passvault.kotlin.insecure-cipher-transformation
    Cipher.getInstance("AES/ECB/PKCS5Padding")

fun safeCipher(): Cipher =
    // ok: passvault.kotlin.insecure-cipher-transformation
    Cipher.getInstance("AES/GCM/NoPadding")

fun weakRandom(): Random =
    // ruleid: passvault.kotlin.weak-random-in-security-code
    java.util.Random()

fun sensitiveLogging(secret: String) {
    // ruleid: passvault.kotlin.sensitive-production-logging
    println(secret)
}

fun interpolatedSql(database: FakeDatabase, userInput: String) {
    // ruleid: passvault.kotlin.interpolated-raw-sql
    database.execSQL("DELETE FROM records WHERE id = $userInput")
}

fun trustAll(): HostnameVerifier =
    // ruleid: passvault.kotlin.trust-all-tls
    HostnameVerifier { _, _ -> true }

interface FakeDatabase {
    fun execSQL(statement: String)
}
