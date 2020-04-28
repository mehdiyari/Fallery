package ir.mehdiyari.fallery.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.FileOutputStream
import java.util.*

fun getCompressFormatBasedOnExtension(ext: String) = when (ext.toLowerCase(Locale.US)) {
    "png" -> Bitmap.CompressFormat.PNG
    "webp" -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}

fun Bitmap.saveBitmapToFile(
    cachePath: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 100
) {
    FileOutputStream(cachePath).also { output ->
        this.compress(format, quality, output)
    }
}

fun MediaMetadataRetriever.extractVideoDimension(videoPath: String): Pair<Int, Int> {
    setDataSource(videoPath)
    return extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt() to extractMetadata(
        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
    ).toInt()
}