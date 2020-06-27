package ir.mehdiyari.fallery.main.di.component

import ir.mehdiyari.fallery.imageLoader.FalleryImageLoader
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider

internal interface FalleryCoreComponent {

    fun provideFalleryOptions(): FalleryOptions

    fun provideImageLoader(): FalleryImageLoader

    fun provideBucketProvider(): AbstractMediaBucketProvider

    fun provideBucketContentProvider(): AbstractBucketContentProvider

    fun releaseCoreComponent()
}