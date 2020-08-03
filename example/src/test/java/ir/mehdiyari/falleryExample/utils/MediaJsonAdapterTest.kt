package ir.mehdiyari.falleryExample.utils

import ir.mehdiyari.fallery.models.Media
import org.junit.Test

class MediaJsonAdapterTest {

    private val json = """
        [
          {
            "type": "photo",
            "id": 12,
            "path": "http//mehdiyari.ir/fallery/4.jpg",
            "width": 1280,
            "height": 960
          },
          {
            "type": "video",
            "path": "http//mehdiyari.ir/fallery/second_video.mp4",
            "duration": 128,
            "thumbnail": {
              "id": 12,
              "path": "http//mehdiyari.ir/fallery/second_video.jpg",
              "width": 1920,
              "height": 1080
            }
          }
        ]
    """.trimIndent()

    @Test
    fun fromJson() {
        val jsonAdapter = MediaJsonAdapter()
        jsonAdapter.fromJson(json)?.apply {
            assert(this.size == 2)
            assert(this.first() is Media.Photo)
            assert(this[1] is Media.Video)
        }
    }
}