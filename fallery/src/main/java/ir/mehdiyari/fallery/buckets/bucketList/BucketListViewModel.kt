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

@OptIn(ExperimentalCoroutinesApi::class)
internal class BucketListViewModel constructor(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType: BucketType = BucketType.VIDEO_PHOTO_BUCKETS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    private val bucketsMutableStateFlow = MutableStateFlow<List<MediaBucket>>(listOf())

    private val allMediaCountChangedStateFlow = MutableStateFlow(0)

    val allMediaCountChanged: StateFlow<Int> = allMediaCountChangedStateFlow

    val bucketsStateFlow: StateFlow<List<MediaBucket>> = this.bucketsMutableStateFlow

    private val loadingMutableStateFlow = MutableStateFlow<LoadingViewState?>(null)

    val loadingViewStateFlow: StateFlow<LoadingViewState?> = loadingMutableStateFlow


    fun getBucketNameById(id: Long): String = bucketsStateFlow.value.firstOrNull {
        id == it.id
    }?.displayName ?: "Unknown"

    fun getBuckets(refresh: Boolean = false) {
        if (!refresh && bucketsStateFlow.value.isNotEmpty() || loadingViewStateFlow.value == LoadingViewState.ShowLoading) return
        loadingMutableStateFlow.value = LoadingViewState.ShowLoading
        viewModelScope.launch(ioDispatcher) {
            try {
                abstractMediaBucketProvider.getMediaBuckets(bucketType).also { buckets ->
                    viewModelScope.launch(Dispatchers.Main) {
                        loadingMutableStateFlow.value = LoadingViewState.HideLoading
                        bucketsMutableStateFlow.value = buckets
                        allMediaCountChangedStateFlow.value = buckets.firstOrNull()?.mediaCount ?: 0
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                Log.e(FALLERY_LOG_TAG, "error while fetching buckets.(${t.message})")
                viewModelScope.launch(Dispatchers.Main) {
                    loadingMutableStateFlow.value = LoadingViewState.HideLoading
                    loadingMutableStateFlow.value = LoadingViewState.Error
                }
            }
        }
    }

    fun retry() {
        getBuckets(true)
    }
}

