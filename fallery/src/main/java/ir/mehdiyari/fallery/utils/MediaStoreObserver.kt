package ir.mehdiyari.fallery.utils

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.ref.WeakReference

@OptIn(ExperimentalCoroutinesApi::class)
internal class MediaStoreObserver constructor(
    handler: Handler,
    val context: WeakReference<FragmentActivity>
) : ContentObserver(handler), LifecycleObserver {

    private val _externalStorageChangeState = SingleLiveEvent<Uri?>()
    val externalStorageChangeState: LiveData<Uri?> = _externalStorageChangeState

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
        _externalStorageChangeState.value = uri
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy() {
        context.get()?.lifecycle?.removeObserver(this)
        context.get()?.contentResolver?.unregisterContentObserver(this)
    }
}