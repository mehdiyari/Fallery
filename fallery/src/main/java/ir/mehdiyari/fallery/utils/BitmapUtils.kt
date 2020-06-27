package ir.mehdiyari.fallery.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun getPhotoDimension(path: String): PhotoDiminution = BitmapFactory.Options().apply {
    inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, this)
}.let {
    PhotoDiminution(it.outWidth, it.outHeight)
}

fun Bitmap.Config.decodeBitmap(path: String): Bitmap? = BitmapFactory.Options()
    .let { options ->
        options.inPreferredConfig = this
        BitmapFactory.decodeFile(path, options)
    }

fun scalePictureToSize(
    sourcePath: String,
    destinationPath: String,
    width: Int,
    height: Int
): File = BitmapFactory.decodeFile(sourcePath).let { sourceBitmap ->
    if (sourceBitmap == null || (sourceBitmap.width < width && sourceBitmap.height < height)) return@let File(sourcePath)
    Bitmap.createScaledBitmap(sourceBitmap, width, height, false).let { destinationBitmap ->
        val destFile = File(destinationPath)
        val outputStream = FileOutputStream(destFile)
        try {
            destinationBitmap.compress(getCompressFormatBasedOnExtension(getFileExtensionFromPath(sourcePath) ?: "jpg"), 100, outputStream)
        } finally {
            outputStream.run {
                flush()
                close()
            }
        }
        destFile
    }
}

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

internal fun getHeightBasedOnScaledWidth(
    originalWidth: Int,
    originalHeight: Int,
    scaledWidth: Int
): Int = (originalHeight / (originalWidth.toFloat() / scaledWidth)).toInt()