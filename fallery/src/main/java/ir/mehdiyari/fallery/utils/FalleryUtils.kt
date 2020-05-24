package ir.mehdiyari.fallery.utils

import android.provider.MediaStore

internal const val FALLERY_LOG_TAG = "Fallery"
internal const val WRITE_EXTERNAL_REQUEST_CODE = 100_000

internal const val videoPhotoBucketSelection =
    """(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND ${MediaStore.MediaColumns.SIZE}>0) GROUP BY (bucket_id"""

internal const val videoPhotoBucketSelectionAndroidQ =
    """(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND ${MediaStore.MediaColumns.SIZE}>0"""

internal const val getSingleBucketSelection =
    """ ${MediaStore.Files.FileColumns.MEDIA_TYPE}=? AND ${MediaStore.MediaColumns.SIZE}>0) GROUP BY (bucket_id"""

internal const val getSingleBucketSelectionAndroidQ =
    """ ${MediaStore.Files.FileColumns.MEDIA_TYPE}=? AND ${MediaStore.MediaColumns.SIZE}>0"""

fun Int.toReadableCount() : String = this.toLong().toReadableCount()

fun Long.toReadableCount() : String = when {
    this < 999L -> this.toString()
    this in 1000L..999999L -> "${this / 1000L}K"
    this > 999999L -> "${this / 1000000L}M"
    else -> "${this / 1000000000L}B"
}