package ir.mehdiyari.fallery.main.di.module

import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponent
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.repo.MediaBucketProvider
import java.lang.NullPointerException

internal class FalleryCoreModule constructor(
    private val falleryOptions: FalleryOptions
) : FalleryCoreComponent {

    private var defaultImageLoader: FalleryImageLoader? = null
    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null
    private var abstractBucketContentProvider: AbstractBucketContentProvider? = null

    override fun provideFalleryOptions(): FalleryOptions = falleryOptions

    override fun provideImageLoader(): FalleryImageLoader = falleryOptions.imageLoader ?: throw NullPointerException("imageLoader must not be null")

    override fun provideBucketProvider(): AbstractMediaBucketProvider =
        falleryOptions.bucketProviderAbstract ?: throw IllegalArgumentException("${MediaBucketProvider::class.simpleName} can provide only in feature component holders")

    override fun provideBucketContentProvider(): AbstractBucketContentProvider = falleryOptions.abstractBucketContentProvider
        ?: throw IllegalArgumentException("${AbstractBucketContentProvider::class.simpleName} can provide only in feature component holders")

    override fun releaseCoreComponent() {
        defaultImageLoader = null
        abstractMediaBucketProvider = null
        abstractBucketContentProvider = null
    }

}