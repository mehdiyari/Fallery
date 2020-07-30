package ir.mehdiyari.fallery.imageLoader

import android.content.Context
import android.widget.ImageView

/**
 * interface for loading photos and gif
 */
interface FalleryImageLoader {

    /**
     * load photo in [path] into [imageView] with [resizeDiminution]
     * this method called for loading thumbnails and photos
     *
     * @param context context
     * @param resizeDiminution requested width and height of photo
     * @param imageView photo load destination
     * @param path path of photo
     */
    fun loadPhoto(
        context: Context,
        imageView: ImageView,
        resizeDiminution: PhotoDiminution,
        path: String
    )

    /**
     * load gif in [path] into [imageView] with [resizeDiminution]
     * this method called for loading gif photos
     *
     * @param context context
     * @param imageView gif load destination
     * @param resizeDiminution requested width and height of photo
     * @param path path of gif photo
     */
    fun loadGif(
        context: Context,
        imageView: ImageView,
        resizeDiminution: PhotoDiminution,
        path: String
    )

}