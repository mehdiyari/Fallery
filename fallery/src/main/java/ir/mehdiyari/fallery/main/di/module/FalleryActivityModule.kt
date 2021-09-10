package ir.mehdiyari.fallery.main.di.module

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.buckets.bucketContent.content.adapter.BucketContentAdapter
import ir.mehdiyari.fallery.buckets.bucketList.adapter.BucketListAdapter
import ir.mehdiyari.fallery.buckets.bucketList.adapter.MediaBucketDiffCallback
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.main.di.component.FalleryActivityComponent
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponent
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.models.CacheDir
import ir.mehdiyari.fallery.models.FalleryStyleAttrs
import ir.mehdiyari.fallery.models.getFalleryStyleAttrs
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.repo.BucketContentProvider
import ir.mehdiyari.fallery.repo.MediaBucketProvider
import ir.mehdiyari.fallery.utils.*
import java.lang.ref.WeakReference

internal class FalleryActivityModule(
    private val context: Context,
    private val falleryActivity: FragmentActivity,
    private val falleryCoreComponent: FalleryCoreComponent
) : FalleryActivityComponent {

    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null
    private var abstractBucketContentProvider: AbstractBucketContentProvider? = null
    private var bucketListViewModelFactory: BucketListViewModelFactory? = null
    private var falleryStyleAttrs: FalleryStyleAttrs? = null
    private var falleryMediaObserver: MediaStoreObserver? = null

    override fun provideBucketListViewModelFactory(): BucketListViewModelFactory =
        synchronized(bucketListViewModelFactory ?: this) {
            if (bucketListViewModelFactory == null) {
                bucketListViewModelFactory =
                    BucketListViewModelFactory(
                        abstractMediaBucketProvider = provideBucketProvider(),
                        bucketType = provideFalleryOptions().mediaTypeFilter,
                        mediaObserverEnabled = provideFalleryOptions().mediaObserverEnabled,
                        mediaStoreObserver = provideMediaStoreObserver()
                    )

                bucketListViewModelFactory!!
            } else {
                bucketListViewModelFactory!!
            }
        }

    override fun provideActivity(): FragmentActivity = falleryActivity

    override fun releaseBucketListComponent() {
        abstractMediaBucketProvider = null
        bucketListViewModelFactory = null
    }

    override fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback =
        MediaBucketDiffCallback()

    override fun provideFalleryOptions(): FalleryOptions = falleryCoreComponent.provideFalleryOptions()
    override fun provideImageLoader(): FalleryImageLoader = falleryCoreComponent.provideImageLoader()
    override fun provideBucketProvider(): AbstractMediaBucketProvider = try {
        falleryCoreComponent.provideBucketProvider()
    } catch (ignored: Throwable) {
        provideFalleryBucketProvider()
    }

    override fun provideBucketContentProvider(): AbstractBucketContentProvider = try {
        falleryCoreComponent.provideBucketContentProvider()
    } catch (ignored: Throwable) {
        provideFalleryBucketContentProvider()
    }


    override fun releaseCoreComponent() = falleryCoreComponent.releaseCoreComponent()

    private fun provideFalleryBucketContentProvider(): AbstractBucketContentProvider =
        synchronized(abstractBucketContentProvider ?: this) {
            if (abstractBucketContentProvider == null) {
                abstractBucketContentProvider = BucketContentProvider(provideContentResolver(), provideCacheDir())
                abstractBucketContentProvider!!
            } else abstractBucketContentProvider!!
        }


    private fun provideFalleryBucketProvider(): AbstractMediaBucketProvider =
        synchronized(abstractMediaBucketProvider ?: this) {
            if (abstractMediaBucketProvider == null) {
                abstractMediaBucketProvider = MediaBucketProvider(provideCacheDir(), provideContentResolver())
                abstractMediaBucketProvider!!
            } else abstractMediaBucketProvider!!
        }

    override fun provideFalleryStyleAttrs(): FalleryStyleAttrs = synchronized(falleryStyleAttrs ?: this) {
        if (falleryStyleAttrs == null) {
            falleryStyleAttrs = falleryActivity.getFalleryStyleAttrs()
            falleryStyleAttrs!!
        } else
            falleryStyleAttrs!!
    }

    override fun provideBucketContentViewModelFactory(): BucketContentViewModelFactory = BucketContentViewModelFactory(
        provideBucketContentProvider(), provideFalleryOptions().mediaTypeFilter
    )

    override fun provideCacheDir(): CacheDir = CacheDir(context.externalCacheDir?.path ?: context.cacheDir.path)

    override fun provideContentResolver(): ContentResolver = context.contentResolver


    override fun provideBucketContentAdapter(): BucketContentAdapter =
        BucketContentAdapter(
            provideImageLoader(),
            provideSelectedDrawable(),
            provideDeselectedDrawable(),
            provideFalleryStyleAttrs().falleryPlaceHolderColor
        )

    override fun provideFalleryViewModelFactory(): FalleryViewModelFactory = FalleryViewModelFactory(
        provideFalleryOptions(), provideMediaStoreObserver()
    )

    override fun provideSelectedDrawable(): Drawable = createCircleDrawableWithStroke(
        provideFalleryOptions().selectedMediaToggleBackgroundColor, dpToPx(2), Color.WHITE
    )!!

    override fun provideDeselectedDrawable(): Drawable = createCircleDrawableWithStroke(
        Color.parseColor("#54000000"), dpToPx(2), Color.WHITE
    )!!

    override fun provideBucketListAdapter(): BucketListAdapter = BucketListAdapter(
        mediaBucketDiffCallback = provideMediaBucketDiffCallback(),
        imageLoader = provideImageLoader(),
        placeHolderColor = provideFalleryStyleAttrs().falleryPlaceHolderColor
    )

    override fun provideMediaStoreObserver(): MediaStoreObserver = synchronized(falleryMediaObserver ?: this) {
        if (falleryMediaObserver == null) {
            falleryMediaObserver = MediaStoreObserver(provideFalleryOptions().mediaObserverEnabled, Handler(Looper.getMainLooper()), WeakReference(provideActivity()))
        }

        falleryMediaObserver!!
    }
}