package ir.mehdiyari.fallery.main.fallery

import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import ir.mehdiyari.fallery.R
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.utils.EnumType

data class FalleryOptions(
    val mediaTypeFilterOptions: MediaTypeFilterOptions,
    val cameraEnabledOptions: CameraEnabledOptions,
    val captionEnabledOptions: CaptionEnabledOptions,
    val mediaCountOptions: MediaCountOptions,
    val imageLoader: FalleryImageLoader?,
    val bucketProviderAbstract: AbstractMediaBucketProvider?,
    val abstractBucketContentProvider: AbstractBucketContentProvider?,
    val falleryStrings: FalleryStrings,
    @StyleRes val themeResId: Int,
    val orientationMode: Int,
    val autoHideMediaPreviewToolbarOnSingleTap: Boolean,
    var bucketRecyclerViewItemMode: BucketRecyclerViewItemMode,
    val changeBucketRecyclerViewItemModeByToolbarIcon: Boolean,
    var mediaObserverEnabled: Boolean
) {
    constructor() : this(
        MediaTypeFilterOptions(),
        CameraEnabledOptions(),
        CaptionEnabledOptions(),
        MediaCountOptions(),
        null,
        null,
        null,
        FalleryStrings(),
        R.style.Fallery_Light,
        13, //SCREEN_ORIENTATION_FULL_USER
        true,
        BucketRecyclerViewItemMode.GridStyle,
        true,
        true
    )
}

const val UNLIMITED_SELECT = -1

data class MediaTypeFilterOptions(
    val bucketType: BucketType,
    val maxSelectablePhoto: Int = UNLIMITED_SELECT,
    val maxSelectableVideo: Int = UNLIMITED_SELECT,
    val maxSelectableMedia: Int = UNLIMITED_SELECT
) {
    constructor() : this(BucketType.VIDEO_PHOTO_BUCKETS)
}

data class CameraEnabledOptions(
    val enabled: Boolean = false,
    val fileProviderAuthority: String? = null,
    val directory: String? = null
)

data class CaptionEnabledOptions(
    val enabled: Boolean = false,
    @DrawableRes val sendIcon: Int = R.drawable.fallery_icon_send,
    @LayoutRes val editTextLayoutResId: Int = R.layout.caption_edit_text_layout
)

data class MediaCountOptions(
    val enabled: Boolean = true,
    val format: String = "%d %s %d %s" // [selected number] of [total number] selected
)

data class FalleryStrings(
    @StringRes val bucketsToolbarTitle: Int = R.string.fallery_toolbar_title,
    @StringRes val captionHint: Int = R.string.fallery_caption_hint_text,
    @StringRes val mediaCount: Int = R.string.media_count
)

enum class BucketRecyclerViewItemMode constructor(override var value: Int) : EnumType<Int> {
    GridStyle(R.layout.grid_bucket_item_view),
    LinearStyle(R.layout.linear_bucket_item_view)
}