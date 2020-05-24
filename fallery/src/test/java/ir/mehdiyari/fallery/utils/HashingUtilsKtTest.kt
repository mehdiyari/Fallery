package ir.mehdiyari.fallery.utils

import encodeToBase85
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class HashingUtilsKtTest {

    @Before
    fun setUp() {
        File("temp.txt").apply {
            if (createNewFile())
                writeText("hello world. this is temp file just for testing")
        }
    }

    @Test
    fun getHashOfFile() {
        Assert.assertEquals(".i(7MGF'^N@Lb\$ZIiGqT", getHashOfFile("temp.txt").encodeToBase85())
    }


    @After
    fun after() {
        File("temp.txt").delete()
    }
}