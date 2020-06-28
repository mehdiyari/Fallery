package ir.mehdiyari.fallery.buckets.ui.bucketList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.utils.BaseViewModel
import ir.mehdiyari.fallery.utils.FALLERY_LOG_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class BucketListViewModel constructor(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType :BucketType = BucketType.VIDEO_PHOTO_BUCKETS
) : BaseViewModel() {

    @ExperimentalCoroutinesApi
    private val bucketsMutableStateFlow = MutableStateFlow<List<MediaBucket>>(listOf())

    @ExperimentalCoroutinesApi
    private val allMediaCountChangedStateFlow = MutableStateFlow(0)

    @ExperimentalCoroutinesApi
    val allMediaCountChanged: StateFlow<Int> = allMediaCountChangedStateFlow

    @ExperimentalCoroutinesApi
    val bucketsStateFlow: StateFlow<List<MediaBucket>> = this.bucketsMutableStateFlow

    private val falleryViewState = MutableLiveData<BucketListViewState>()
    val bucketListViewStateLiveData: LiveData<BucketListViewState> = falleryViewState

    @ExperimentalCoroutinesApi
    fun getBuckets() {
        if (bucketsMutableStateFlow.value.isNotEmpty() ||  falleryViewState.value == BucketListViewState.ShowLoading) return
        falleryViewState.value = BucketListViewState.ShowLoading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                abstractMediaBucketProvider.getMediaBuckets(bucketType).also { buckets ->
                    falleryViewState.postValue(BucketListViewState.HideLoading)
                    viewModelScope.launch(Dispatchers.Main) {
                        bucketsMutableStateFlow.value = buckets
                        allMediaCountChangedStateFlow.value = buckets.firstOrNull()?.mediaCount ?: 0
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                Log.e(FALLERY_LOG_TAG, "error while fetching buckets.(${t.message})")
                falleryViewState.postValue(BucketListViewState.ErrorInFetchingBuckets)
            }
        }
    }
}

