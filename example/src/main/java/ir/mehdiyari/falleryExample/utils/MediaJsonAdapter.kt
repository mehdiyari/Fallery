package ir.mehdiyari.falleryExample.utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import ir.mehdiyari.fallery.models.Media
import java.lang.reflect.Type


class MediaJsonAdapter : JsonAdapter<List<Media>>() {

    private val typeOptions = JsonReader.Options.of("type")
    private val photoOptions = JsonReader.Options.of("id", "path", "width", "height")
    private val videoOptions = JsonReader.Options.of("id", "path", "duration", "thumbnail")

    override fun fromJson(reader: JsonReader): List<Media>? {
        val mediasList = mutableListOf<Media>()
        reader.beginArray()

        while (reader.hasNext()) {
            reader.beginObject()

            if (reader.selectName(typeOptions) == 0) {
                when (val type = reader.nextString()) {
                    "photo" -> mediasList.add(getPhotoFromReader(reader))
                    "video" -> mediasList.add(getVideoFromReader(reader))
                    else -> {
                        throw IllegalArgumentException("no type with name $type")
                    }
                }
            } else {
                reader.skipName()
                reader.skipValue()
            }

            reader.endObject()
        }

        reader.endArray()
        return mediasList.toList()
    }

    private fun getPhotoFromReader(reader: JsonReader): Media.Photo {
        var id = 0L
        var path = ""
        var width = 0
        var height = 0
        while (reader.hasNext()) {
            when (reader.selectName(photoOptions)) {
                0 -> id = reader.nextLong()
                1 -> path = reader.nextString()
                2 -> width = reader.nextInt()
                3 -> height = reader.nextInt()
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }

        return Media.Photo(id, path, width, height)
    }

    private fun getVideoFromReader(reader: JsonReader): Media.Video {
        var id = 0L
        var path = ""
        var duration = 0L
        var thumbnail: Media.Photo? = null
        while (reader.hasNext()) {
            when (reader.selectName(videoOptions)) {
                0 -> id = reader.nextLong()
                1 -> path = reader.nextString()
                2 -> duration = reader.nextLong()
                3 -> {
                    reader.beginObject()
                    thumbnail = getPhotoFromReader(reader)
                    reader.endObject()
                }
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }

        return Media.Video(id, path, duration, thumbnail!!)
    }

    override fun toJson(writer: JsonWriter, value: List<Media>?) {
        TODO("Not yet implemented")
    }
}

class MediaJsonAdapterFactory : Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return if (type.toString().contains("java.util.List<? extends ir.mehdiyari.fallery.models.Media>")) {
            return MediaJsonAdapter()
        } else {
            null
        }
    }

}
