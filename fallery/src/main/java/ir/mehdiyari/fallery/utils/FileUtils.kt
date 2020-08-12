package ir.mehdiyari.fallery.utils

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