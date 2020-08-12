package ir.mehdiyari.fallery.utils

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import java.lang.ref.WeakReference

internal class MediaStoreObserver constructor(
    handler: Handler,
    val context: WeakReference<AppCompatActivity>
) : ContentObserver(handler), LifecycleObserver {

    private val externalStorageChangeMutableLiveData = MutableLiveData<Uri>()
    val externalStorageChangeLiveData: LiveData<Uri> = externalStorageChangeMutableLiveData

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun registerObservers() {
        context.get()?.lifecycle?.addObserver(this)
        context.get()?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this
        )

        context.get()?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, this
        )

        context.get()?.contentResolver?.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, this
        )

        context.get()?.contentResolver?.registerContentObserver(
            MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, this
        )
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        externalStorageChangeMutableLiveData.value = uri
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy() {
        context.get()?.lifecycle?.removeObserver(this)
        context.get()?.contentResolver?.unregisterContentObserver(this)
    }
}