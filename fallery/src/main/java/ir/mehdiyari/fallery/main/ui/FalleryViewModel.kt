package ir.mehdiyari.fallery.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.mehdiyari.fallery.main.fallery.BucketRecyclerViewItemMode
import ir.mehdiyari.fallery.utils.SingleLiveEvent

class FalleryViewModel : ViewModel() {

    val currentFragment = SingleLiveEvent<FalleryView>()

    init {
        currentFragment.value = FalleryView.BucketList
    }


    private val bucketRecyclerViewMode = MutableLiveData<BucketRecyclerViewItemMode>()
    val bucketRecycleViewModeLiveData: LiveData<BucketRecyclerViewItemMode> = bucketRecyclerViewMode
    private val storagePermissionGrantedStateMutableLiveData = MutableLiveData<Boolean>()
    val storagePermissionGrantedStateLiveData: LiveData<Boolean> = storagePermissionGrantedStateMutableLiveData

    fun changeRecyclerViewItemMode(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        bucketRecyclerViewMode.value = bucketRecyclerViewItemMode
    }

    fun storagePermissionGranted() {
        storagePermissionGrantedStateMutableLiveData.value = true
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