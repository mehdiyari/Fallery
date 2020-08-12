package ir.mehdiyari.fallery.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import androidx.annotation.ColorInt
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import java.io.FileOutputStream

internal fun getPhotoDimension(path: String): PhotoDiminution = BitmapFactory.Options().apply {
    inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, this)
}.let {
    PhotoDiminution(it.outWidth, it.outHeight)
}

internal fun Bitmap.saveBitmapToFile(
    cachePath: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 100
) {
    FileOutputStream(cachePath).also { output ->
        this.compress(format, quality, output)
    }
}

internal fun MediaMetadataRetriever.getVideoSize(): PhotoDiminution = PhotoDiminution(
    extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt(), extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
)

internal fun getHeightBasedOnScaledWidth(
    originalWidth: Int,
    originalHeight: Int,
    scaledWidth: Int
): Int = (originalHeight / (originalWidth.toFloat() / scaledWidth)).toInt()

internal fun createCircleDrawableWithStroke(
    @ColorInt backgroundColor: Int,
    strokeWidth: Int,
    @ColorInt strokeColor: Int
): Drawable? {
    val defaultDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(backgroundColor, backgroundColor))
    defaultDrawable.cornerRadius = 300f
    defaultDrawable.setStroke(strokeWidth, strokeColor)
    return defaultDrawable
}