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
import kotlinx.coroutines.*
import java.lang.ref.WeakReference

internal class MediaStoreObserver constructor(
    isMediaObserverEnabled: Boolean = false,
    handler: Handler,
    val context: WeakReference<FragmentActivity>
) : ContentObserver(handler), LifecycleObserver {

    private val _externalStorageChangeState = SingleLiveEvent<Uri?>()
    val externalStorageChangeState: LiveData<Uri?> = _externalStorageChangeState
    private var latestURI: Uri? = null
    private var postValueJob: Job? = null

    companion object {
        const val DELAY_FOR_NOTIFY_OBSERVERS = 2000L
    }

    init {
        if (isMediaObserverEnabled) {
            context.get()?.lifecycle?.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
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

        postValueJob?.cancel()
        postValueJob = null

        postValueJob = GlobalScope.launch(Dispatchers.Default) {
            delay(DELAY_FOR_NOTIFY_OBSERVERS)
            if (isActive) {
                _externalStorageChangeState.postValue(uri)
                latestURI = uri
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObservers() {
        postValueJob?.cancel()
        postValueJob = null
        latestURI = null
        context.get()?.lifecycle?.removeObserver(this)
        context.get()?.contentResolver?.unregisterContentObserver(this)
    }
}