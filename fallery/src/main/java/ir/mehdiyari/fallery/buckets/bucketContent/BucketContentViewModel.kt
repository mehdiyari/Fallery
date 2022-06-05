package ir.mehdiyari.fallery.buckets.bucketContent

import androidx.lifecycle.LiveData
import ir.mehdiyari.fallery.buckets.bucketList.LoadingViewState
import ir.mehdiyari.fallery.models.BucketType
import ir.mehdiyari.fallery.models.Media
import ir.mehdiyari.fallery.repo.AbstractBucketContentProvider
import ir.mehdiyari.fallery.utils.BUCKET_CONTENT_DEFAULT_SPAN_COUNT
import ir.mehdiyari.fallery.utils.BaseViewModel
import ir.mehdiyari.fallery.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class BucketContentViewModel constructor(
    private val abstractBucketContentProvider: AbstractBucketContentProvider,
    private val bucketType: BucketType,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    private val medias = MutableStateFlow<List<Media>>(listOf())
    val mediaList: StateFlow<List<Media>> = medias

    private val _showPreviewFragmentLiveData = SingleLiveEvent<String>()
    val showPreviewFragmentLiveData: LiveData<String> = _showPreviewFragmentLiveData

    private val loadingMutableStateFlow = MutableStateFlow<LoadingViewState?>(null)
    val loadingViewStateFlow: StateFlow<LoadingViewState?> = loadingMutableStateFlow

    private val _spanCountStateFlow = MutableStateFlow<BucketContentSpanCount?>(null)
    val spanCountStateFlow: StateFlow<BucketContentSpanCount?> = _spanCountStateFlow

    fun getMedias(bucketId: Long, refresh: Boolean = false) {
        if (!refresh && mediaList.value.isNotEmpty()) return
        val clearList = AtomicBoolean(refresh)
        loadingMutableStateFlow.value = LoadingViewState.ShowLoading
        viewModelScope.launch(ioDispatcher) {
            abstractBucketContentProvider.getMediasOfBucket(bucketId, bucketType)
                .catch {
                    viewModelScope.launch(Dispatchers.Main) {
                        loadingMutableStateFlow.value = LoadingViewState.Error
                    }
                }
                .collect {
                    if (medias.value.isEmpty()) {
                        viewModelScope.launch(Dispatchers.Main) {
                            loadingMutableStateFlow.value = LoadingViewState.HideLoading
                        }
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
        _showPreviewFragmentLiveData.value = path
    }

    fun getIndexOfPath(path: String): Int =
        mediaList.value.indexOfFirst { it.getMediaPath() == path.trim() }

    fun getMediaPathByPosition(position: Int): String? =
        mediaList.value.getOrNull(position)?.getMediaPath()

    fun retry(bucketId: Long) {
        getMedias(bucketId, true)
    }

    fun changeSpanCountBasedOnUserTouch(
        zoomIn: Boolean,
        maxSpanCount: Int,
        minSpanCount: Int,
        spanCount: Int?,
        isPortrait: Boolean
    ) {
        val span = spanCount ?: BUCKET_CONTENT_DEFAULT_SPAN_COUNT
        val newSpan = if (!zoomIn && span < maxSpanCount) {
            span + 1
        } else if (zoomIn && span > minSpanCount) {
            span - 1
        } else {
            span
        }

        viewModelScope.launch {
            _spanCountStateFlow.value.also { spanCountState ->
                if (spanCountState != null) {
                    _spanCountStateFlow.emit(
                        if (isPortrait)
                            spanCountState.copy(portraitSpanCount = newSpan)
                        else
                            spanCountState.copy(landScapeSpanCount = newSpan)
                    )
                } else {
                    _spanCountStateFlow.emit(
                        if (isPortrait) {
                            BucketContentSpanCount(
                                portraitSpanCount = newSpan,
                                landScapeSpanCount = newSpan
                            )
                        } else {
                            BucketContentSpanCount(
                                portraitSpanCount = BUCKET_CONTENT_DEFAULT_SPAN_COUNT,
                                landScapeSpanCount = newSpan
                            )
                        }
                    )
                }
            }
        }
    }
}