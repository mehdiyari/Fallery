package ir.mehdiyari.fallery.repo

import android.content.ContentResolver
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.CacheDir
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

internal class BucketContentProvider constructor(
    private val contentResolver: ContentResolver,
    private val cacheDir: CacheDir
) : AbstractBucketContentProvider {

    override suspend fun getMediasOfBucket(bucketId: Long, bucketType: BucketType): Flow<List<Media>> = flow {
        val medias = mutableListOf<Media>()
        val mediaMetadataRetriever = MediaMetadataRetriever()
        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            photoWithVideoProjection,
            "${getSimpleQueryByMediaType(bucketType)} ${if (bucketId == ALL_MEDIA_MODEL_ID) "" else "AND bucket_id=?"}",
            getQueryArgsForGetContentBuckets(bucketType, bucketId),
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            if (cursor.count == 0) {
                emit(listOf<Media>())
            } else {
                while (cursor.moveToNext()) {
                    medias.add(getMediaFromCursor(cursor, mediaMetadataRetriever))
                    // emit medias as chunks with size 100
                    if (medias.size >= 100) {
                        emit(medias.toList())
                        medias.clear()
                    }
                }
            }

            emit(medias.toList())
            medias.clear()
        }
    }

    private fun getMediaFromCursor(
        cursor: Cursor,
        mediaMetadataRetriever: MediaMetadataRetriever
    ): Media {
        val isPhoto = cursor.getInt(cursor.getColumnIndex(photoWithVideoProjection[4])) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        val path = cursor.getString(cursor.getColumnIndexOrThrow(photoWithVideoProjection[1]))
        if (!isPhoto) mediaMetadataRetriever.setDataSource(path)

        val thumbnail = if (!isPhoto)
            createThumbForVideosOrEmpty(listOf(Pair(path, cursor.getLong(cursor.getColumnIndex(photoWithVideoProjection[5])))), cacheDir.cacheDir).firstOrNull()
        else ""

        val id = cursor.getLong(cursor.getColumnIndex(photoWithVideoProjection[0]))

        return if (isPhoto) {
            getPhotoDimension(path).let {
                Media.Photo(id, path, it.width, it.height)
            }
        } else {
            val thumbnailDiminution = try {
                getPhotoDimension(thumbnail ?: "")
            } catch (ignored: Throwable) {
                PhotoDiminution(0, 0)
            }

            Media.Video(
                id, path, (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong() / 1000), Media.Photo(
                    Random.nextLong(), thumbnail ?: "", thumbnailDiminution.width, thumbnailDiminution.height
                )
            )
        }
    }

    private fun getSimpleQueryByMediaType(mediaType: BucketType): String = when (mediaType) {
        BucketType.VIDEO_PHOTO_BUCKETS -> "(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?)"
        else -> "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
    }

    private fun getQueryArgsForGetContentBuckets(bucketType: BucketType, bucketId: Long): Array<String> {
        val arrays = mutableListOf<String>()
        arrays.addAll(getQueryArgByMediaType(bucketType))
        if (bucketId != ALL_MEDIA_MODEL_ID)
            arrays.add(bucketId.toString())

        return arrays.toTypedArray()
    }
}