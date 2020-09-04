package ir.mehdiyari.fallery.main.fallery

import android.content.pm.ActivityInfo
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.viewpager2.widget.ViewPager2
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider

/**
 * Builder for [FalleryOptions]
 */
class FalleryBuilder constructor(private var falleryOptions: FalleryOptions = FalleryOptions(null)) {

    /**
     * filter buckets that contains [bucketType]
     */
    fun mediaTypeFiltering(
        bucketType: BucketType
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaTypeFilter = bucketType
        )
        return this
    }

    /**
     * set [maxSelectableMedia] as max media count selected by user. default value is [UNLIMITED_SELECT]
     */
    fun setMaxSelectableMedia(
        maxSelectableMedia: Int
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            maxSelectableMedia = maxSelectableMedia
        )
        return this
    }


    /**
     * enable or disable taking photo from camera in fallery.
     * if you enable camera you must set [CameraEnabledOptions.fileProviderAuthority]
     */
    fun setCameraEnabledOptions(
        cameraEnabledOptions: CameraEnabledOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            cameraEnabledOptions = cameraEnabledOptions
        )
        return this
    }

    /**
     * enable or disable sending caption with medias. and customize editText and send icon.
     *
     * note: if you are using customEditText(like emojiEdiText) in your app you can pass
     * editText as layout res to [CaptionEnabledOptions.editTextLayoutResId]. its important
     * your customEditText is root of layout file and id must be equal to [R.id.falleryEditTextCaption]
     * @return FalleryBuilder
     */
    fun setCaptionEnabledOptions(
        captionEnabledOptions: CaptionEnabledOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            captionEnabledOptions = captionEnabledOptions
        )
        return this
    }

    /**
     * enable or disable showing media count
     */
    fun setMediaCountEnabled(
        enable: Boolean
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(mediaCountEnabled = enable)
        return this
    }

    /**
     * set current theme of fallery. fallery support dracula [R.style.Fallery_Dracula]
     * and light [R.style.Fallery_Light] theme internally. but you can create and set your own theme.
     * @param theme Int style res of fallery theme
     */
    fun setTheme(
        @StyleRes theme: Int
    ): FalleryBuilder {

        falleryOptions = falleryOptions.copy(themeResId = theme)
        return this
    }

    /**
     * set orientation of FalleryActivity. default value is [ActivityInfo.SCREEN_ORIENTATION_USER]
     * @param orientationMode Int require ActivityInfo.ScreenOrientation constants
     */
    fun setOrientation(
        orientationMode: Int
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            orientationMode = orientationMode
        )
        return this
    }

    /**
     * fallery does not use image loading libraries like glide, picasso internally.
     * you must implement [FalleryImageLoader] and load requested photo or gif into their image view.
     * with glide, picasso or custom image loader
     * @param imageLoader FalleryImageLoader implement of [FalleryImageLoader]
     */
    fun setImageLoader(
        imageLoader: FalleryImageLoader
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            imageLoader = imageLoader
        )

        return this
    }

    /**
     * set your custom bucket provider by implement [AbstractMediaBucketProvider] and
     * set your custom bucket content provider by implement [AbstractBucketContentProvider]
     * note: if you want create custom gallery with fallery please check it http://mehdiyari.ir/?s=Fallery
     * @param bucketContentProvider AbstractBucketContentProvider custom implementation of [AbstractBucketContentProvider]
     */
    fun setContentProviders(
        bucketContentProvider: AbstractBucketContentProvider,
        bucketProvider: AbstractMediaBucketProvider
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            abstractBucketContentProvider = bucketContentProvider,
            bucketProviderAbstract = bucketProvider
        )

        return this
    }

    /**
     * set default [bucketRecyclerViewItemMode]. you can enable or disable
     * changing bucket item mode by user with [setBucketItemModeToggleEnabled]
     */
    fun setBucketItemMode(
        bucketRecyclerViewItemMode: BucketRecyclerViewItemMode
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            bucketRecyclerViewItemMode = bucketRecyclerViewItemMode
        )
        return this
    }

    /**
     * enable or disable changing bucket item mode by user
     */
    fun setBucketItemModeToggleEnabled(
        enable: Boolean
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            bucketItemModeToggleEnabled = enable
        )
        return this
    }

    /**
     * if new media add or remove in the device, fallery reload the buckets or bucket content. by defaults
     * media observer is disabled if you want to enable it just pass true to [setMediaObserverEnabled]
     */
    fun setMediaObserverEnabled(enable: Boolean): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaObserverEnabled = enable
        )

        return this
    }

    /**
     * set toolbar title text of falleryActivity
     */
    fun setFalleryToolbarTitleText(@StringRes titleRes: Int): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            toolbarTitle = titleRes
        )

        return this
    }

    /**
     * set orientation for photo preview viewPager
     */
    fun setMediaPreviewViewPagerOrientation(@ViewPager2.Orientation orientation: Int): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaPreviewScrollOrientation = orientation
        )

        return this
    }

    /**
     * set pagerTransformer for photo preview viewPager
     */
    fun setMediaPreviewPageTransformer(pageTransformer: ViewPager2.PageTransformer?): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaPreviewPageTransformer = pageTransformer
        )

        return this
    }

    /**
     * set toggle background color when media selected
     */
    fun setSelectedMediaToggleBackgroundColor(
        color: Int
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            selectedMediaToggleBackgroundColor = color
        )

        return this
    }

    /**
     * set onClick listener for video play toggle
     */
    fun setOnVideoPlayClick(
        onClick: (path: String) -> Unit
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            onVideoPlayClick = onClick
        )

        return this
    }

    /**
     * if true fallery request external storage permission from user
     */
    fun setGrantExternalStoragePermission(
        grantExternalStoragePermission: Boolean
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            grantExternalStoragePermission = grantExternalStoragePermission
        )

        return this
    }

    fun build(): FalleryOptions {
        require(falleryOptions.imageLoader != null) { "You must set imageLoader" }

        if (falleryOptions.cameraEnabledOptions.enabled)
            require(falleryOptions.cameraEnabledOptions.fileProviderAuthority != null) { "fileProviderAuthority must not be null" }

        return falleryOptions
    }
}