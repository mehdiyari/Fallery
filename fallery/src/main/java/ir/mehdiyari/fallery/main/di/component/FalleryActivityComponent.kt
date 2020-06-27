package ir.mehdiyari.fallery.main.di.component

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.buckets.ui.bucketContent.adapter.BucketContentAdapter
import ir.mehdiyari.fallery.buckets.ui.bucketList.adapter.MediaBucketDiffCallback
import ir.mehdiyari.fallery.models.FalleryStyleAttrs
import ir.mehdiyari.fallery.utils.BucketListViewModelFactory
import ir.mehdiyari.fallery.utils.FalleryViewModelFactory

internal interface FalleryActivityComponent : FalleryCoreComponent {

    fun provideApplicationContext(): Context

    fun provideBucketListViewModelFactory(): BucketListViewModelFactory

    fun provideActivity(): FragmentActivity

    fun releaseBucketListComponent()

    fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback

    fun provideBucketContentAdapter(): BucketContentAdapter

    fun provideFalleryStyleAttrs(): FalleryStyleAttrs

    fun provideFalleryViewModelFactory(): FalleryViewModelFactory
}