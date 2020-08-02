package ir.mehdiyari.fallery.buckets.bucketContent

import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.utils.BaseViewModel
import ir.mehdiyari.fallery.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


internal class BucketContentViewModel constructor(
    private val abstractBucketContentProvider: AbstractBucketContentProvider,
    private val bucketType: BucketType,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    @ExperimentalCoroutinesApi
    private val medias = MutableStateFlow<List<Media>>(listOf())

    @ExperimentalCoroutinesApi
    val mediaList: StateFlow<List<Media>> = medias

    val showPreviewFragmentLiveData = SingleLiveEvent<String>()

    @ExperimentalCoroutinesApi
    private val loadingMutableStateFlow = MutableStateFlow<LoadingViewState?>(null)

    @ExperimentalCoroutinesApi
    val loadingViewStateFlow: StateFlow<LoadingViewState?> = loadingMutableStateFlow

    @ExperimentalCoroutinesApi
    fun getMedias(bucketId: Long, refresh: Boolean = false) {
        if (!refresh && mediaList.value.isNotEmpty()) return
        val clearList = AtomicBoolean(refresh)
        loadingMutableStateFlow.value = LoadingViewState.ShowLoading
        viewModelScope.launch(ioDispatcher) {
            abstractBucketContentProvider.getMediasOfBucket(bucketId, bucketType)
                .catch {
                    viewModelScope.launch(Dispatchers.Main) { loadingMutableStateFlow.value = LoadingViewState.Error }
                }
                .collect {
                    if (medias.value.isEmpty()) {
                        viewModelScope.launch(Dispatchers.Main) { loadingMutableStateFlow.value = LoadingViewState.HideLoading }
                    }

                    if (clearList.get()) {
                        clearList.compareAndSet(true, false)
                        medias.value = it
                    } else {
                        medias.value = mediaList.value.toMutableList().apply { addAll(it) }.toList()
                    }
                }
        }
    }

    fun showPreviewFragment(path: String) {
        showPreviewFragmentLiveData.value = path
    }

    @ExperimentalCoroutinesApi
    fun getIndexOfPath(path: String): Int = mediaList.value.indexOfFirst { it.getMediaPath() == path.trim() }

    @ExperimentalCoroutinesApi
    fun getMediaPathByPosition(position: Int): String? = mediaList.value.getOrNull(position)?.getMediaPath()

    fun retry(bucketId: Long) {
        getMedias(bucketId, true)
    }
}