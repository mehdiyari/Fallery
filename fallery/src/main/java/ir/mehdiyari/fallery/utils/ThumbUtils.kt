package ir.mehdiyari.fallery.utils

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.WorkerThread
import ir.mehdiyari.fallery.imageLoader.PhotoDiminution
import java.io.File


@WorkerThread
internal fun createThumbForVideos(
    videosPath: List<Pair<String, Long>>,
    cacheDir: String,
    highQuality: Pair<Boolean, PhotoDiminution?> = false to null
): List<String> = mutableListOf<String>().apply {
    videosPath.forEach {
        val cachePath =
            "$cacheDir/${File(it.first).nameWithoutExtension}__${it.second}.jpg"
        if (File(cachePath).exists())
            add(cachePath)
        else {
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    File(it.first),
                    if (!highQuality.first) Size(512, 384) else Size(highQuality.second!!.width, highQuality.second!!.height),
                    null
                )
            } else {
                ThumbnailUtils.createVideoThumbnail(
                    it.first,
                    if (highQuality.first) MediaStore.Images.Thumbnails.FULL_SCREEN_KIND else MediaStore.Images.Thumbnails.MINI_KIND
                )
            })?.apply {
                saveBitmapToFile(
                    cachePath, Bitmap.CompressFormat.JPEG, 75
                )
            }

            add(cachePath)
        }
    }
}.toList()

internal fun createThumbForVideosOrEmpty(
    videosPath: List<Pair<String, Long>>,
    cacheDir: String,
    highQuality: Pair<Boolean, PhotoDiminution?> = false to null
): List<String> = try {
    createThumbForVideos(videosPath, cacheDir, highQuality)
} catch (ignored: Throwable) {
    ignored.printStackTrace()
    listOf("")
}