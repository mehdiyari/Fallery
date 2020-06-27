package ir.mehdiyari.fallery.imageLoader

import android.content.Context
import android.widget.ImageView

/**
 * interface for loading photos and gif
 * default implementation of this without using external libraries such(glide, picasso) implemented
 */
interface FalleryImageLoader {

    /**
     * load photo in path [path] into [imageView] with size [resizeDiminution]
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
     * load gif in path [path] into [imageView] with size [resizeDiminution]
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