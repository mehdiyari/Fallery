package ir.mehdiyari.fallery.utils

import android.provider.MediaStore
import ir.mehdiyari.fallery.models.BucketType


val bucketProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    "bucket_id",
    "bucket_display_name",
    MediaStore.MediaColumns.DATA,
    MediaStore.MediaColumns.MIME_TYPE,
    "COUNT(*) AS count",
    "datetaken"
)

internal val photoWithVideoProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.DATA,
    MediaStore.Files.FileColumns.SIZE,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.Files.FileColumns.DATE_ADDED
)


val bucketProjectionAndroidQ = arrayOf(
    MediaStore.Files.FileColumns._ID,
    "bucket_id",
    "bucket_display_name",
    MediaStore.MediaColumns.DATA,
    MediaStore.MediaColumns.MIME_TYPE,
    "datetaken"
)

internal fun getQueryByMediaType(mediaType: BucketType): String = when (mediaType) {
    BucketType.VIDEO_PHOTO_BUCKETS -> if (isAndroidTenOrHigher()) videoPhotoBucketSelectionAndroidQ else videoPhotoBucketSelection
    else -> if (isAndroidTenOrHigher()) getSingleBucketSelectionAndroidQ else getSingleBucketSelection
}

internal fun getQueryArgByMediaType(mediaType: BucketType): Array<String> = when (mediaType) {
    BucketType.VIDEO_PHOTO_BUCKETS -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(), MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
    BucketType.ONLY_PHOTO_BUCKETS -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
    BucketType.ONLY_VIDEO_BUCKETS -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
}