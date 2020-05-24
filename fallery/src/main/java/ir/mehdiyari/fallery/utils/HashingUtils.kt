package ir.mehdiyari.fallery.utils

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * get hash of file
 * @param filePath path of file
 * @param algorithm hashing algorithm(MD5, Sha-256, Sha-512)
 */
internal fun getHashOfFile(filePath: String, algorithm: String = "MD5"): ByteArray = try {
    ByteArray(DEFAULT_BUFFER_SIZE).let { buffer ->
        MessageDigest.getInstance(algorithm).let { digest ->
            BufferedInputStream(FileInputStream(filePath)).use {
                while (true) {
                    val count = it.read(buffer)
                    if (count == -1)
                        break
                    else
                        digest.update(buffer, 0, count)
                }
            }

            digest.digest()
        }
    }
} catch (t:Throwable) {
    t.printStackTrace()
    ByteArray(0)
}