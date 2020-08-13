package ir.mehdiyari.fallery.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.mehdiyari.fallery.buckets.bucketContent.BucketContentViewModel
import ir.mehdiyari.fallery.buckets.bucketList.BucketListViewModel
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.ui.FalleryViewModel
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider

internal class BucketListViewModelFactory(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType: BucketType
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(BucketListViewModel::class.java)) {
            BucketListViewModel(abstractMediaBucketProvider, bucketType) as T
        } else throw IllegalArgumentException("this factory is just for BucketListViewModel")
}

internal class FalleryViewModelFactory(
    private val falleryOptions: FalleryOptions
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = if (modelClass.isAssignableFrom(FalleryViewModel::class.java)) {
        FalleryViewModel(falleryOptions) as T
    } else throw IllegalArgumentException("this factory is just for FalleryViewModel")

}


class BucketContentViewModelFactory constructor(
    private val abstractBucketContentProvider: AbstractBucketContentProvider,
    private val bucketType: BucketType
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = if (modelClass.isAssignableFrom(BucketContentViewModel::class.java)) {
        BucketContentViewModel(
            abstractBucketContentProvider, bucketType
        ) as T
    } else throw IllegalArgumentException("this factory is just for BucketContentViewModel")
}