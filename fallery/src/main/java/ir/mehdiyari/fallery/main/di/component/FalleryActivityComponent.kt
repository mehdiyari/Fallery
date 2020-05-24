package ir.mehdiyari.fallery.main.di.component

import android.content.Context
import androidx.fragment.app.FragmentActivity
import ir.mehdiyari.fallery.buckets.ui.bucketList.adapter.MediaBucketDiffCallback
import ir.mehdiyari.fallery.models.FalleryStyleAttrs
import ir.mehdiyari.fallery.utils.BucketListViewModelFactory

internal interface FalleryActivityComponent : FalleryCoreComponent {

    fun provideApplicationContext(): Context

    fun provideBucketListViewModelFactory(): BucketListViewModelFactory

    fun provideActivity(): FragmentActivity

    fun releaseBucketListComponent()

    fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback

    fun provideFalleryStyleAttrs(): FalleryStyleAttrs
}