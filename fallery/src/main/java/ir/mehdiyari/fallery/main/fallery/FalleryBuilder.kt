package ir.mehdiyari.fallery.main.fallery

import androidx.annotation.StyleRes
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import java.lang.IllegalArgumentException

class FalleryBuilder {

    private var falleryOptions = FalleryOptions()

    fun mediaTypeFiltering(
        mediaTypeFilterOptions: MediaTypeFilterOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaTypeFilterOptions = mediaTypeFilterOptions
        )
        return this
    }

    fun cameraEnabled(
        cameraEnabledOptions: CameraEnabledOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            cameraEnabledOptions = cameraEnabledOptions
        )
        return this
    }

    fun captionEnabled(
        captionEnabledOptions: CaptionEnabledOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            captionEnabledOptions = captionEnabledOptions
        )
        return this
    }

    fun mediaCountEnabled(
        mediaCountOptions: MediaCountOptions
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(mediaCountOptions = mediaCountOptions)
        return this
    }

    fun theme(
        @StyleRes theme: Int
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(themeResId = theme)
        return this
    }

    fun setOrientation(
        orientationMode: Int
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            orientationMode = orientationMode
        )
        return this
    }

    fun setImageLoader(
        imageLoader: FalleryImageLoader
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            imageLoader = imageLoader
        )
        return this
    }

    fun setAutoHideMediaPreviewToolbarOnSingleTap(
        enable: Boolean
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            autoHideMediaPreviewToolbarOnSingleTap = enable
        )
        return this
    }

    fun falleryStrings(
        falleryStrings: FalleryStrings
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            falleryStrings = falleryStrings
        )
        return this
    }

    fun bucketProvider(
        bucketProviderAbstract: AbstractMediaBucketProvider
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            bucketProviderAbstract = bucketProviderAbstract
        )
        return this
    }

    fun bucketContentProvider(
        bucketContentProvider: AbstractBucketContentProvider
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            abstractBucketContentProvider = bucketContentProvider
        )

        return this
    }

    fun setBucketItemMode(
        bucketRecyclerViewItemMode: BucketRecyclerViewItemMode
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            bucketRecyclerViewItemMode = bucketRecyclerViewItemMode
        )
        return this
    }

    fun setChangeBucketRecyclerViewItemModeByToolbarIcon(
        enable: Boolean
    ): FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            changeBucketRecyclerViewItemModeByToolbarIcon = enable
        )
        return this
    }

    fun setMediaObserverEnabled(enable: Boolean) : FalleryBuilder {
        falleryOptions = falleryOptions.copy(
            mediaObserverEnabled = enable
        )

        return this
    }

    fun build(): FalleryOptions {
        if (falleryOptions.imageLoader == null) throw IllegalArgumentException("You must set imageLoader")

        if (falleryOptions.cameraEnabledOptions.enabled)
            require(falleryOptions.cameraEnabledOptions.fileProviderAuthority != null) { "fileProviderAuthority must not be null" }

        return falleryOptions
    }
}