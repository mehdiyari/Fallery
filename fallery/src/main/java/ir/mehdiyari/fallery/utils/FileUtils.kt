package ir.mehdiyari.fallery.utils

import android.content.Context

fun getFileExtensionFromPath(url: String): String? = try {
    url.lastIndexOf('.').let {
        if (it != -1)
            url.substring(it + 1)
        else
            null
    }
} catch (ignored: Throwable) {
    null
}

fun Context.getWritableCachePath(): String? = when {
    this.externalCacheDir?.canWrite() == true -> this.externalCacheDir?.path
    this.cacheDir?.canWrite() == true -> this.cacheDir?.path
    else -> null
}

fun Context.getReadableCachePath(): String? = when {
    this.externalCacheDir?.canRead() == true -> this.externalCacheDir?.path
    this.cacheDir?.canRead() == true -> this.cacheDir?.path
    else -> null
}