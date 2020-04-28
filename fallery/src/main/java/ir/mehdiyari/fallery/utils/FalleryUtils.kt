package ir.mehdiyari.fallery.utils

import android.provider.MediaStore

internal const val FALLERY_LOG_TAG = "Fallery"
internal const val WRITE_EXTERNAL_REQUEST_CODE = 100_000

internal const val videoPhotoBucketSelection =
    """(${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?) AND ${MediaStore.MediaColumns.SIZE}>0) GROUP BY (bucket_id"""

internal const val getSingleBucketSelection =
    """ ${MediaStore.Files.FileColumns.MEDIA_TYPE}=? AND ${MediaStore.MediaColumns.SIZE}>0) GROUP BY (bucket_id"""
