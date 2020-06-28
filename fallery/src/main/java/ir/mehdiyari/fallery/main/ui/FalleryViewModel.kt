package ir.mehdiyari.fallery.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.utils.SingleLiveEvent

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

    private val storagePermissionGrantedStateMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val storagePermissionGrantedStateFlow: StateFlow<Boolean?> = storagePermissionGrantedStateMutableStateFlow
    private val bucketRecyclerViewMode = MutableLiveData<BucketRecyclerViewItemMode>()
    val bucketRecycleViewModeLiveData: LiveData<BucketRecyclerViewItemMode> = bucketRecyclerViewMode
    private val storagePermissionGrantedStateMutableLiveData = MutableLiveData<Boolean>()
    val storagePermissionGrantedStateLiveData: LiveData<Boolean> = storagePermissionGrantedStateMutableLiveData

    fun changeRecyclerViewItemMode(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        bucketRecyclerViewMode.value = bucketRecyclerViewItemMode
    }

    fun openBucketWithId(it: Long) {
        currentFragmentLiveData.value = FalleryView.BucketContent(it)

    }

    fun storagePermissionGranted() {
        storagePermissionGrantedStateMutableStateFlow.value = true
    }

    fun openBucketWithId(it: Long) {
        currentFragment.value = FalleryView.BucketContent(it)
    }
}

sealed class FalleryView {
    object BucketList : FalleryView()
    data class BucketContent(
        val bucketId: Long
    ) : FalleryView()

    object PhotoPreview : FalleryView()
}