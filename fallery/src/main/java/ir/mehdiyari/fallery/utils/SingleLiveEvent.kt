package ir.mehdiyari.fallery.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

internal class SingleLiveEvent<T> : MutableLiveData<T?>() {
    private val mPending = AtomicBoolean(false)

    @MainThread
    fun observeSingleEvent(owner: LifecycleOwner?, observer: Observer<T>) {
        // Observe the internal MutableLiveData
        super.observe(owner!!, Observer { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }
}