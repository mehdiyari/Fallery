package ir.mehdiyari.fallery.utils

import android.provider.MediaStore


val bucketProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    "bucket_id",
    "bucket_display_name",
    MediaStore.MediaColumns.DATA,
    MediaStore.MediaColumns.MIME_TYPE,
    "COUNT(*) AS count",
    "datetaken"
)