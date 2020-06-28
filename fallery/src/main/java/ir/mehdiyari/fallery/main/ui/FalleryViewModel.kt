package ir.mehdiyari.fallery.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.main.fallery.FalleryOptions
import ir.mehdiyari.fallery.main.fallery.UNLIMITED_SELECT
import ir.mehdiyari.fallery.utils.BaseViewModel
import ir.mehdiyari.fallery.utils.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
internal class FalleryViewModel(
    private val falleryOptions: FalleryOptions
) : BaseViewModel() {

    val currentFragmentLiveData = SingleLiveEvent<FalleryView>()

    val userSelectedMedias: Boolean
        get() = mediaSelectionTracker.isNotEmpty()

    val mediaSelectionTracker = mutableListOf<String>()
    var totalMediaCount = 0
        set(value) {
            field = if (falleryOptions.mediaTypeFilterOptions.maxSelectableMedia == UNLIMITED_SELECT) value else falleryOptions.mediaTypeFilterOptions.maxSelectableMedia
        }
        get() = if (falleryOptions.mediaTypeFilterOptions.maxSelectableMedia == UNLIMITED_SELECT)  field else falleryOptions.mediaTypeFilterOptions.maxSelectableMedia

    init {
        currentFragmentLiveData.value = FalleryView.BucketList
    }

    private val captionEnabledMutableLiveData = MutableLiveData<Boolean>()
    val captionEnabledLiveData = captionEnabledMutableLiveData
    private val storagePermissionGrantedStateMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val storagePermissionGrantedStateFlow: StateFlow<Boolean?> = storagePermissionGrantedStateMutableStateFlow
    private val bucketRecyclerViewMode = MutableLiveData<BucketRecyclerViewItemMode>()
    val bucketRecycleViewModeLiveData: LiveData<BucketRecyclerViewItemMode> = bucketRecyclerViewMode
    private val mediaCountMutableStateFlow = MutableStateFlow(MediaCountModel(0, 0))
    val mediaCountStateFlow: StateFlow<MediaCountModel> = mediaCountMutableStateFlow

    private val allMediaDeselectedMutableStateFlow = MutableStateFlow(false)
    val allMediaDeselectedStateFlow : Flow<Boolean> = allMediaDeselectedMutableStateFlow

    init {
        if (falleryOptions.captionEnabledOptions.enabled && mediaSelectionTracker.isNotEmpty())
            captionEnabledMutableLiveData.value = falleryOptions.captionEnabledOptions.enabled
    }

    fun changeRecyclerViewItemMode(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        bucketRecyclerViewMode.value = bucketRecyclerViewItemMode
    }

    fun openBucketWithId(it: Long) {
        currentFragmentLiveData.value = FalleryView.BucketContent(it)

    }

    fun storagePermissionGranted() {
        storagePermissionGrantedStateMutableStateFlow.value = true
    }

    fun requestDeselectingMedia(path: String): Boolean {
        if (!mediaSelectionTracker.contains(path)) return false
        return mediaSelectionTracker.remove(path).also {
            if (falleryOptions.captionEnabledOptions.enabled && it && mediaSelectionTracker.isEmpty())
                captionEnabledMutableLiveData.value = false

            if (falleryOptions.mediaCountOptions.enabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun requestSelectingMedia(path: String): Boolean {
        mediaSelectionTracker.remove(path)
        if (falleryOptions.mediaTypeFilterOptions.maxSelectableMedia != UNLIMITED_SELECT && falleryOptions.mediaTypeFilterOptions.maxSelectableMedia ==  mediaSelectionTracker.size) {
            return false
        }

        return mediaSelectionTracker.add(path).also {
            if (falleryOptions.captionEnabledOptions.enabled && it && mediaSelectionTracker.size == 1)
                captionEnabledMutableLiveData.value = true

            if (falleryOptions.mediaCountOptions.enabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun deselectAllSelections() {
        mediaSelectionTracker.clear()
        allMediaDeselectedMutableStateFlow.value = true
        allMediaDeselectedMutableStateFlow.value = false
        if (falleryOptions.captionEnabledOptions.enabled)
            captionEnabledMutableLiveData.value = false

        if (falleryOptions.mediaCountOptions.enabled)
            mediaCountMutableStateFlow.value = MediaCountModel(0, totalMediaCount)
    }
}