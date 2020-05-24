package ir.mehdiyari.fallery.utils

import android.util.Log
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

abstract class AbstractFeatureComponentHolder<T> {

    private var component: T? = null
    private var activity : WeakReference<FragmentActivity>? = null

    fun setComponent(component: T) {
        if (this.component == null) this.component = component
    }

    fun createOrGetComponent(activity: FragmentActivity): T {
        if (this.component == null) component = componentCreator(activity).also {
            this.activity = WeakReference(activity)
        }

        return component!!
    }

    abstract fun componentCreator(activity: FragmentActivity): T

    open fun onDestroy() {
        try {
            Log.d(FALLERY_LOG_TAG, "${this::class.simpleName} has been destroyed")
            this.component = null
            Runtime.getRuntime().gc()
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
    }

    fun getOrNull(): T? = this.component
}