package ir.mehdiyari.fallery.buckets.ui.bucketContent

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


class BucketContentViewModel constructor(
    private val abstractBucketContentProvider: AbstractBucketContentProvider,
    private val bucketType: BucketType,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    @ExperimentalCoroutinesApi
    private val medias = MutableStateFlow<List<Media>>(listOf())

    @ExperimentalCoroutinesApi
    val mediaList: StateFlow<List<Media>> = medias

    @ExperimentalCoroutinesApi
    fun getMedias(bucketId: Long, refresh: Boolean = false) {
        if (!refresh && mediaList.value.isNotEmpty()) return
        val clearList = AtomicBoolean(refresh)
        viewModelScope.launch(ioDispatcher) {
            abstractBucketContentProvider.getMediasOfBucket(bucketId, bucketType)
                .collect {
                    if (clearList.get()) {
                        clearList.compareAndSet(true, false)
                        medias.value = it
                    } else {
                        medias.value = mediaList.value.toMutableList().apply { addAll(it) }.toList()
                    }
                }
        }
    }
}