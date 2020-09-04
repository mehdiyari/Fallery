package ir.mehdiyari.fallery.main.di.component

import android.content.ContentResolver
import android.graphics.drawable.Drawable
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.buckets.bucketContent.content.adapter.BucketContentAdapter
import ir.mehdiyari.fallery.buckets.bucketList.adapter.BucketListAdapter
import ir.mehdiyari.fallery.buckets.bucketList.adapter.MediaBucketDiffCallback
import ir.mehdiyari.fallery.models.CacheDir
import ir.mehdiyari.fallery.models.FalleryStyleAttrs
import ir.mehdiyari.fallery.utils.BucketContentViewModelFactory
import ir.mehdiyari.fallery.utils.BucketListViewModelFactory
import ir.mehdiyari.fallery.utils.FalleryViewModelFactory
import ir.mehdiyari.fallery.utils.MediaStoreObserver

internal interface FalleryActivityComponent : FalleryCoreComponent {

    fun provideBucketListViewModelFactory(): BucketListViewModelFactory

    fun provideActivity(): FragmentActivity

    fun releaseBucketListComponent()

    fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback

    fun provideBucketContentAdapter(): BucketContentAdapter

    fun provideBucketContentViewModelFactory(): BucketContentViewModelFactory

    fun provideFalleryStyleAttrs(): FalleryStyleAttrs

    fun provideCacheDir(): CacheDir

    fun provideContentResolver(): ContentResolver

    fun provideFalleryViewModelFactory(): FalleryViewModelFactory

    fun provideSelectedDrawable(): Drawable

    fun provideDeselectedDrawable(): Drawable

    fun provideBucketListAdapter(): BucketListAdapter

    fun provideMediaStoreObserver(): MediaStoreObserver
}