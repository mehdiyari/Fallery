package ir.mehdiyari.fallery.buckets.bucketList

import android.util.Log
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.utils.BaseViewModel
import ir.mehdiyari.fallery.utils.FALLERY_LOG_TAG
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class BucketListViewModel constructor(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType: BucketType = BucketType.VIDEO_PHOTO_BUCKETS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    @ExperimentalCoroutinesApi
    private val bucketsMutableStateFlow = MutableStateFlow<List<MediaBucket>>(listOf())

    @ExperimentalCoroutinesApi
    private val allMediaCountChangedStateFlow = MutableStateFlow(0)

    @ExperimentalCoroutinesApi
    val allMediaCountChanged: StateFlow<Int> = allMediaCountChangedStateFlow

    @ExperimentalCoroutinesApi
    val bucketsStateFlow: StateFlow<List<MediaBucket>> = this.bucketsMutableStateFlow

    @ExperimentalCoroutinesApi
    private val falleryMutableViewState = MutableStateFlow<BucketListViewState?>(null)

    @ExperimentalCoroutinesApi
    val bucketListViewStateFlow: StateFlow<BucketListViewState?> = falleryMutableViewState

    @ExperimentalCoroutinesApi
    fun getBuckets(refresh: Boolean = false) {
        if (!refresh && bucketsStateFlow.value.isNotEmpty() || bucketListViewStateFlow.value == BucketListViewState.ShowLoading) return
        falleryMutableViewState.value = BucketListViewState.ShowLoading
        viewModelScope.launch(ioDispatcher) {
            try {
                abstractMediaBucketProvider.getMediaBuckets(bucketType).also { buckets ->
                    viewModelScope.launch(Dispatchers.Main) {
                        falleryMutableViewState.value = BucketListViewState.HideLoading
                        bucketsMutableStateFlow.value = buckets
                        allMediaCountChangedStateFlow.value = buckets.firstOrNull()?.mediaCount ?: 0
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                Log.e(FALLERY_LOG_TAG, "error while fetching buckets.(${t.message})")
                viewModelScope.launch(Dispatchers.Main) {
                    falleryMutableViewState.value = BucketListViewState.HideLoading
                    falleryMutableViewState.value = BucketListViewState.ErrorInFetchingBuckets
                }
            }
        }
    }
}

