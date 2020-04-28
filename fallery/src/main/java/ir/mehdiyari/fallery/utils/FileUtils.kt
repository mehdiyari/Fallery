package ir.mehdiyari.fallery.utils

import android.content.Context

fun getFileExtensionFromPath(url:String): String? = try {
    url.substring(url.lastIndexOf('.') + 1).let { if (it.isEmpty()) null else it }
} catch (ignored: Throwable) {
    null
}

fun Context.getWritableCachePath() : String? = when {
    this.externalCacheDir?.canWrite() == true -> this.externalCacheDir?.path
    this.cacheDir?.canWrite() == true -> this.cacheDir?.path
    else -> null
}

fun Context.getReadableCachePath() : String? = when {
    this.externalCacheDir?.canRead() == true -> this.externalCacheDir?.path
    this.cacheDir?.canRead() == true -> this.cacheDir?.path
    else -> null
}