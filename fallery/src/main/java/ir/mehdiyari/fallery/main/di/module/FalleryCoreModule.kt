package ir.mehdiyari.fallery.main.di.module

import ir.mehdiyari.fallery.imageLoader.DefaultImageLoader
import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.di.component.FalleryCoreComponent
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.repo.MediaBucketProvider

internal class FalleryCoreModule constructor(
    private val falleryOptions: FalleryOptions
) : FalleryCoreComponent {

    private var defaultImageLoader: FalleryImageLoader? = null
    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null

    override fun provideFalleryOptions(): FalleryOptions = falleryOptions

    override fun provideImageLoader(): FalleryImageLoader = if (falleryOptions.imageLoader == null) {
        defaultImageLoader = DefaultImageLoader()
        defaultImageLoader!!
    } else
        falleryOptions.imageLoader

    override fun provideBucketProvider(): AbstractMediaBucketProvider =
        falleryOptions.bucketProviderAbstract ?: throw IllegalArgumentException("${MediaBucketProvider::class.simpleName} can provide only in feature component holders")

    override fun releaseCoreComponent() {
        defaultImageLoader = null
        abstractMediaBucketProvider = null
    }

}