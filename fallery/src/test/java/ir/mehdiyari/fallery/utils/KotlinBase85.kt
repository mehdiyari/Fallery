package ir.mehdiyari.fallery.utils

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

/**
 * github gist -> https://gist.github.com/mehdiyari/dd2569de8a272a9146898d98d737a7d6
 * simple Base85/Ascii85 encoder-decoder for kotlin
 * @see <a href="Base85">https://en.wikipedia.org/wiki/Ascii85</a>
 */

private const val ascii85Shift = 33
private val base85Pow = intArrayOf(1, 85, 85 * 85, 85 * 85 * 85, 85 * 85 * 85 * 85)
private val removeWhiteSpacePattern = Pattern.compile("\\s+")

/**
 *
 * encoding [ByteArray] to Base85
 */
fun ByteArray.encodeToBase85(): String = StringBuilder(this.size * 5 / 4).also { stringBuilder ->
    ByteArray(4).also { chunk ->
        var chunkIndex = 0
        for (i in this.indices) {
            chunk[chunkIndex++] = this[i]
            if (chunkIndex == 4) {
                val value = byteToInt(chunk)
                if (value == 0)
                    stringBuilder.append('z')
                else
                    stringBuilder.append(encodeChunk(value))

                Arrays.fill(chunk, 0.toByte())
                chunkIndex = 0
            }
        }

        if (chunkIndex > 0) {
            val numPadded = chunk.size - chunkIndex
            Arrays.fill(chunk, chunkIndex, chunk.size, 0.toByte())
            val value = byteToInt(chunk)
            val encodedChunk = encodeChunk(value)
            for (i in 0 until encodedChunk.size - numPadded)
                stringBuilder.append(encodedChunk[i])

        }
    }
}.toString()

/**
 * decoding [ByteArray] from Base85
 */
fun String.decodeFromBase85(): ByteArray {
    var cpValue: String = this
    val decodedLength = BigDecimal.valueOf(cpValue.length.toLong()).multiply(BigDecimal.valueOf(4)).divide(BigDecimal.valueOf(5))
    return ByteBuffer.allocate(decodedLength.toInt()).also { byteBuff ->
        cpValue = removeWhiteSpacePattern.matcher(cpValue).replaceAll("")
        val payload = cpValue.toByteArray(StandardCharsets.US_ASCII)
        val chunk = ByteArray(5)
        var chunkIndex = 0
        for (i in payload.indices) {
            val currByte = payload[i]
            if (currByte == 'z'.toByte()) {
                require(chunkIndex <= 0) { "The payload is not base 85 encoded." }
                chunk[chunkIndex++] = '!'.toByte()
                chunk[chunkIndex++] = '!'.toByte()
                chunk[chunkIndex++] = '!'.toByte()
                chunk[chunkIndex++] = '!'.toByte()
                chunk[chunkIndex++] = '!'.toByte()
            } else {
                chunk[chunkIndex++] = currByte
            }

            if (chunkIndex == 5) {
                byteBuff.put(decodeChunk(chunk))
                Arrays.fill(chunk, 0.toByte())
                chunkIndex = 0
            }
        }

        if (chunkIndex > 0) {
            val numPadded = chunk.size - chunkIndex
            Arrays.fill(chunk, chunkIndex, chunk.size, 'u'.toByte())
            val paddedDecode = decodeChunk(chunk)
            for (i in 0 until paddedDecode.size - numPadded) {
                byteBuff.put(paddedDecode[i])
            }
        }

        byteBuff.flip()
    }.let {
        Arrays.copyOf(it.array(), it.limit())
    }
}

/**
 * encode Int to CharArray
 */
private fun encodeChunk(value: Int): CharArray {
    var longValue = value.toLong() and 0x00000000ffffffffL
    val encodedChunk = CharArray(5)
    encodedChunk.forEachIndexed { index, _ ->
        encodedChunk[index] = (longValue / base85Pow[4 - index] + ascii85Shift).toChar()
        longValue %= base85Pow[4 - index]
    }

    return encodedChunk
}

/**
 * decode Int to charArray
 */
private fun decodeChunk(chunk: ByteArray): ByteArray {
    require(chunk.size == 5) { "You can only decode chunks of size 5." }
    var value = 0
    value += (chunk[0] - ascii85Shift) * base85Pow[4]
    value += (chunk[1] - ascii85Shift) * base85Pow[3]
    value += (chunk[2] - ascii85Shift) * base85Pow[2]
    value += (chunk[3] - ascii85Shift) * base85Pow[1]
    value += (chunk[4] - ascii85Shift) * base85Pow[0]

    return intToByte(value)
}

private fun byteToInt(value: ByteArray?): Int {
    require(!(value == null || value.size != 4)) { "You cannot create an int without exactly 4 bytes." }
    return ByteBuffer.wrap(value).int
}

private fun intToByte(value: Int): ByteArray = byteArrayOf(value.ushr(24).toByte(), value.ushr(16).toByte(), value.ushr(8).toByte(), value.toByte())