package ir.mehdiyari.fallery.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.WorkerThread
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileOutputStream


@WorkerThread
fun createThumbForVideos(
    videosPath: List<Pair<String, Long>>,
    context: Context
): List<String> = mutableListOf<String>().apply {
    videosPath.forEach {
        val cachePath =
            "${context.externalCacheDir ?: context.cacheDir}/${File(it.first).nameWithoutExtension}__${it.second}.jpg"
        if (File(cachePath).exists())
            add(cachePath)
        else {
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    File(it.first),
                    Size(512, 384),
                    null
                )
            } else {
                ThumbnailUtils.createVideoThumbnail(
                    it.first,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
            }).apply {
                this.saveBitmapToFile(
                    cachePath, Bitmap.CompressFormat.JPEG, 75
                )
            }

            add(cachePath)
        }
    }
}