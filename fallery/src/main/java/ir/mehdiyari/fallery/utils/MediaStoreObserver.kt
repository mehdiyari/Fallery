package ir.mehdiyari.fallery.utils

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

internal class MediaStoreObserver constructor(
    handler: Handler,
    val context: FragmentActivity
) : ContentObserver(handler), LifecycleObserver {

    private val externalStorageChangeMutableLiveData = MutableLiveData<Uri>()
    val externalStorageChangeLiveData: LiveData<Uri> = externalStorageChangeMutableLiveData

    init {
        context.lifecycle.addObserver(this)
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this
        )

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, this
        )

        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, this
        )

        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, this
        )
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        externalStorageChangeMutableLiveData.value = uri
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        context.lifecycle.removeObserver(this)
        context.contentResolver.unregisterContentObserver(this)
    }
}