package ir.mehdiyari.fallery.utils

import org.junit.Test

import org.junit.Assert.*

class FileUtilsKtTest {

    @Test
    fun getFileExtensionFromPath() {
        assertEquals("mp4", getFileExtensionFromPath("/storage/emulated/0/Downloads/121324654_VID.mp4"))
        assertEquals(null, getFileExtensionFromPath("/storage/emulated/0/Downloads/"))
    }
}