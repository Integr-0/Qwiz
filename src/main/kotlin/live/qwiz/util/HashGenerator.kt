package live.qwiz.util

import java.security.MessageDigest
import java.util.UUID

class HashGenerator {
    companion object {
        fun generateHash(): String {
            return digest512(UUID.randomUUID().toString())
        }

        fun digest512(input: String): String {
            val messageDigest = MessageDigest.getInstance("SHA-512")
            val digest = messageDigest.digest(input.toByteArray())
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }

        fun validate512(hash: String): Boolean {
            return Regex("[a-fA-F0-9]{32,128}").matches(hash)
        }
    }
}