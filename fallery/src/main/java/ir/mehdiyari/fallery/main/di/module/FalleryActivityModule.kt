package ir.mehdiyari.fallery.main.di.module

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.main.di.component.FalleryActivityComponent
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponent
import ir.mehdiyari.fallery.buckets.ui.bucketList.adapter.MediaBucketDiffCallback
import ir.mehdiyari.fallery.models.FalleryStyleAttrs
import ir.mehdiyari.fallery.models.getFalleryStyleAttrs
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.repo.BucketContentProvider
import ir.mehdiyari.fallery.repo.MediaBucketProvider
import ir.mehdiyari.fallery.utils.BucketListViewModelFactory

internal class FalleryActivityModule(
    private val context: Context,
    private val falleryActivity: FragmentActivity,
    private val falleryCoreComponent: FalleryCoreComponent
) : FalleryActivityComponent {

    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null
    private var abstractBucketContentProvider: AbstractBucketContentProvider? = null
    private var bucketListViewModelFactory: BucketListViewModelFactory? = null
    private var falleryStyleAttrs: FalleryStyleAttrs? = null

    override fun provideApplicationContext(): Context = context

    override fun provideBucketListViewModelFactory(): BucketListViewModelFactory =
        synchronized(bucketListViewModelFactory ?: this) {
            if (bucketListViewModelFactory == null) {
                bucketListViewModelFactory =
                    BucketListViewModelFactory(provideBucketProvider(), provideFalleryOptions().mediaTypeFilterOptions.bucketType)
                bucketListViewModelFactory!!
            } else bucketListViewModelFactory!!
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
    } catch (ignored:Throwable) {
        provideFalleryBucketProvider()
    }

    override fun provideBucketContentProvider(): AbstractBucketContentProvider = try {
        falleryCoreComponent.provideBucketContentProvider()
    } catch (ignored:Throwable) {
        provideFalleryBucketContentProvider()
    }


    override fun releaseCoreComponent() = falleryCoreComponent.releaseCoreComponent()

    private fun provideFalleryBucketContentProvider(): AbstractBucketContentProvider =
        synchronized(abstractBucketContentProvider ?: this) {
            if (abstractBucketContentProvider == null) {
                abstractBucketContentProvider = BucketContentProvider(context)
                abstractBucketContentProvider!!
            } else abstractBucketContentProvider!!
        }


    private fun provideFalleryBucketProvider(): AbstractMediaBucketProvider =
        synchronized(abstractMediaBucketProvider ?: this) {
            if (abstractMediaBucketProvider == null) {
                abstractMediaBucketProvider = MediaBucketProvider(context)
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
}