package ir.mehdiyari.fallery.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.widget.ImageView
import ir.mehdiyari.fallery.utils.*
import kotlinx.coroutines.*
import java.io.File

internal class DefaultImageLoader : FalleryImageLoader {

    private val imageLoaderScope = CoroutineScope(Dispatchers.IO + CoroutineName("DefaultImageLoaderCoroutine"))
    private val mainScope = MainScope()

    override fun loadPhoto(
        context: Context,
        imageView: ImageView,
        resizeDiminution: PhotoDiminution,
        path: String
    ) {
        (getPhotoDimension(path) != resizeDiminution).also { shouldResizePhoto ->
            if (shouldResizePhoto) {
                imageLoaderScope.launch {
                    val fileHash = Base64.encodeToString(getHashOfFile(path), Base64.URL_SAFE).replace("==", "")
                    File(
                        "${context.getWritableCachePath()}/" +
                                "$fileHash${resizeDiminution.width}_${resizeDiminution.height}." +
                                "${getFileExtensionFromPath(path)}"
                    ).also { file ->
                        if (file.exists())
                            decodeImageAndSetIntoImageView(file.path, imageView)
                        else
                            resizePhotoAndSet(path, file.path, resizeDiminution, imageView)
                    }
                }
            } else
                decodeImageAndSetIntoImageView(path, imageView)

        }
    }

    override fun loadGif(
        context: Context,
        imageView: ImageView,
        resizeDiminution: PhotoDiminution,
        path: String
    ) {
        TODO("not implemented yet")
    }

    private fun resizePhotoAndSet(
        path: String,
        resizedImagePath: String,
        resizeDiminution: PhotoDiminution,
        imageView: ImageView
    ) {
        scalePictureToSize(
            sourcePath = path,
            destinationPath = resizedImagePath,
            width = resizeDiminution.width,
            height = resizeDiminution.height
        ).also { scaledPhoto ->
            if (scaledPhoto.exists())
                decodeImageAndSetIntoImageView(scaledPhoto.path, imageView)
        }
    }

    private fun decodeImageAndSetIntoImageView(
        path: String,
        imageView: ImageView
    ) {
        imageLoaderScope.launch {
            val bitmap = Bitmap.Config.ARGB_8888.decodeBitmap(path = path)
            mainScope.launch {
                imageView.setImageBitmap(bitmap)
            }.join()
        }
    }

    /**
     * must be called when fallery distorted for avoid memory leaks
     */
    fun onDestroy() {
        mainScope.cancel()
        imageLoaderScope.cancel()
    }
}