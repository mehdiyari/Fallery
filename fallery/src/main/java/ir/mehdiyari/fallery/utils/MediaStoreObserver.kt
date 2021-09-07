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
    private var latestURI: Uri? = null

    init {
        context.get()?.lifecycle?.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun registerObservers() {
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
        if (latestURI == uri) return
        _externalStorageChangeState.value = uri
        latestURI = uri
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy() {
        latestURI = null
        context.get()?.lifecycle?.removeObserver(this)
        context.get()?.contentResolver?.unregisterContentObserver(this)
    }
}