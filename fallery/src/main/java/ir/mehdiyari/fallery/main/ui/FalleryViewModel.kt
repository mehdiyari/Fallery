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
import java.io.File

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
            field = if (falleryOptions.maxSelectableMedia == UNLIMITED_SELECT) value else falleryOptions.maxSelectableMedia
        }
        get() = if (falleryOptions.maxSelectableMedia == UNLIMITED_SELECT) field else falleryOptions.maxSelectableMedia

    init {
        currentFragmentLiveData.value = FalleryView.BucketList
    }

    private val captionEnabledMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val captionEnabledStateFlow: StateFlow<Boolean?> = captionEnabledMutableStateFlow
    private val sendActionEnabledMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val sendActionEnabledStateFlow: StateFlow<Boolean?> = sendActionEnabledMutableStateFlow
    private val storagePermissionGrantedStateMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val storagePermissionGrantedStateFlow: StateFlow<Boolean?> = storagePermissionGrantedStateMutableStateFlow
    private val bucketRecyclerViewMode = MutableLiveData<BucketRecyclerViewItemMode>()
    val bucketRecycleViewModeLiveData: LiveData<BucketRecyclerViewItemMode> = bucketRecyclerViewMode
    private val mediaCountMutableStateFlow = MutableStateFlow(MediaCountModel(0, 0))
    val mediaCountStateFlow: StateFlow<MediaCountModel> = mediaCountMutableStateFlow
    private val allMediaDeselectedMutableStateFlow = MutableStateFlow(false)
    val allMediaDeselectedStateFlow: Flow<Boolean> = allMediaDeselectedMutableStateFlow
    private var captionOrSendActionState = true
    private var cameraTemporaryFilePath: String? = null

    val resultSingleLiveEvent = SingleLiveEvent<Array<String>>()

    init {
        if (mediaSelectionTracker.isNotEmpty() && captionOrSendActionState) {
            if (falleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = true
            else
                sendActionEnabledMutableStateFlow.value = true
        }
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
            if (it && mediaSelectionTracker.isEmpty() && captionOrSendActionState) {
                if (falleryOptions.captionEnabledOptions.enabled)
                    captionEnabledMutableStateFlow.value = false
                else
                    sendActionEnabledMutableStateFlow.value = false
            }

            if (falleryOptions.mediaCountEnabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun requestSelectingMedia(path: String): Boolean {
        if (falleryOptions.maxSelectableMedia != UNLIMITED_SELECT && falleryOptions.maxSelectableMedia == mediaSelectionTracker.size) {
            showErrorSingleLiveEvent.value = R.string.fallery_error_max_selectable
            return false
        }

        return mediaSelectionTracker.add(path).also {
            if (it && mediaSelectionTracker.size == 1 && captionOrSendActionState) {
                if (falleryOptions.captionEnabledOptions.enabled)
                    captionEnabledMutableStateFlow.value = true
                else
                    sendActionEnabledMutableStateFlow.value = true
            }

            if (falleryOptions.mediaCountEnabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun deselectAllSelections() {
        mediaSelectionTracker.clear()
        allMediaDeselectedMutableStateFlow.value = true
        allMediaDeselectedMutableStateFlow.value = false

        if (captionOrSendActionState) {
            if (falleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = false
            else
                sendActionEnabledMutableStateFlow.value = false
        }

        if (falleryOptions.mediaCountEnabled)
            mediaCountMutableStateFlow.value = MediaCountModel(0, totalMediaCount)
    }

    fun isPhotoSelected(path: String): Boolean = mediaSelectionTracker.contains(path)

    fun showSendOrCaptionLayout() {
        captionOrSendActionState = true
        if (mediaSelectionTracker.isNotEmpty()) {
            if (falleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = true
            else
                sendActionEnabledMutableStateFlow.value = true
        }
    }

    fun hideSendOrCaptionLayout() {
        captionOrSendActionState = false
        if (falleryOptions.captionEnabledOptions.enabled)
            captionEnabledMutableStateFlow.value = false
        else
            sendActionEnabledMutableStateFlow.value = false
    }

    fun clearCameraPhotoFileAddress() {
        cameraTemporaryFilePath = null
    }

    fun prepareSelectedResults() {
        resultSingleLiveEvent.value = mediaSelectionTracker.toTypedArray()
    }

    fun setCameraPhotoFileAddress(path: String) {
        cameraTemporaryFilePath = path
    }

    fun validateSelections() {
        val validatedList = mediaSelectionTracker.filter { File(it).exists() }
        mediaSelectionTracker.clear()
        mediaSelectionTracker.addAll(validatedList)
        mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        if (mediaSelectionTracker.isEmpty()) {
            if (falleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = false
            else
                sendActionEnabledMutableStateFlow.value = false
        }
    }

    fun prepareCameraResultWithSelectedResults() {
        if (cameraTemporaryFilePath != null) {
            resultSingleLiveEvent.value = mediaSelectionTracker.apply { add(cameraTemporaryFilePath!!) }.toTypedArray()
        }
    }
}