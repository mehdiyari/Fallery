package ir.mehdiyari.fallery.utils

import android.media.MediaMetadataRetriever
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class FalleryUtilsKtTest {

    @Test
    fun ` Given 765 - when call toReadableCount - then return "765" `() {
        assertThat(765.toReadableCount()).isEqualTo("765")
    }

    @Test
    fun ` Given 500_000 - when call toReadableCount - then return 500K `() {
        assertThat(500_000.toReadableCount()).isEqualTo("500K")
    }

    @Test
    fun ` Given 653_352_532 - when call toReadableCount - then return 653M `() {
        assertThat(653_352_532.toReadableCount()).isEqualTo("653M")
    }

    @Test
    fun `Given 73 seconds - when call convertSecondToTime - return 1min 13seconds`() {
        assertThat(convertSecondToTime(73)).isEqualTo("01:13")
    }

    @Test
    fun `Given 3673 seconds - when call convertSecondToTime - return 1hour 1min 13seconds`() {
        assertThat(convertSecondToTime(3673)).isEqualTo("01:01:13")
    }

    @Test
    fun `when call autoClose - then verify release called`() {
        val mockedMediaMetadataRetriever: MediaMetadataRetriever = mockk()
        every { mockedMediaMetadataRetriever.release() } returns Unit
        mockedMediaMetadataRetriever.autoClose { println("do some works...") }
        verify(exactly = 1) { mockedMediaMetadataRetriever.release() }
    }
}