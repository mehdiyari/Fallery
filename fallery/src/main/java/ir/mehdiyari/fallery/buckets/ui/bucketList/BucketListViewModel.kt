package ir.mehdiyari.fallery.buckets.ui.bucketList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.MediaBucket
import ir.mehdiyari.fallery.repo.AbstractMediaBucketProvider
import ir.mehdiyari.fallery.utils.FALLERY_LOG_TAG
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

internal class BucketListViewModel constructor(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType :BucketType = BucketType.VIDEO_PHOTO_BUCKETS
) : BaseViewModel() {

    @ExperimentalCoroutinesApi
    private val bucketsMutableStateFlow = MutableStateFlow<List<MediaBucket>>(listOf())

    @ExperimentalCoroutinesApi
    val bucketsStateFlow: StateFlow<List<MediaBucket>> = this.bucketsMutableStateFlow

    private val falleryViewState = MutableLiveData<BucketListViewState>()
    val bucketListViewStateLiveData: LiveData<BucketListViewState> = falleryViewState

    private val viewModelScope by lazy {
        object : CoroutineScope {
            override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
        }
    }

    @ExperimentalCoroutinesApi
    fun getBuckets() {
        if (bucketsMutableStateFlow.value.isNotEmpty()) return
        falleryViewState.value = BucketListViewState.ShowLoading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                abstractMediaBucketProvider.getMediaBuckets(bucketType).also { buckets ->
                    falleryViewState.postValue(BucketListViewState.HideLoading)
                    bucketsMutableStateFlow.value = buckets
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                Log.e(FALLERY_LOG_TAG, "error while fetching buckets.(${t.message})")
                falleryViewState.postValue(BucketListViewState.ErrorInFetchingBuckets)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}

