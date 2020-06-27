package ir.mehdiyari.fallery.models

sealed class Media {
    fun getMediaId(): Long = when(this) {
        is Photo -> id
        is Video -> id
    }

    fun getMediaPath(): String = when(this) {
        is Photo -> path
        is Video -> path
    }

    data class Photo(
        val id: Long,
        val path: String,
        val width: Int,
        val height: Int
    ) : Media()

    data class Video(
        val id: Long,
        val path: String,
        val duration: Long,
        val thumbnail: Photo
    ) : Media()

}