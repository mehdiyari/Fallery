package ir.mehdiyari.fallery.main.fallery

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import ir.mehdiyari.fallery.main.ui.FalleryActivity
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder

object Fallery {

    fun startFalleryInActivity(requestCode: Int, activity: Activity, falleryOptions: FalleryOptions) {
        FalleryCoreComponentHolder.createComponent(falleryOptions)
        activity.startActivityForResult(Intent(activity, FalleryActivity::class.java), requestCode)
    }

    fun startFalleryInFragment(requestCode: Int, fragment: Fragment, falleryOptions: FalleryOptions) {
        FalleryCoreComponentHolder.createComponent(falleryOptions)
        fragment.startActivityForResult(Intent(fragment.requireContext(), FalleryActivity::class.java), requestCode)
    }

}

fun Activity.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) = Fallery.startFalleryInActivity(requestCode, this, falleryOptions)
fun Fragment.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) = Fallery.startFalleryInFragment(requestCode, this, falleryOptions)
fun Activity.startFallery(requestCode: Int) = Fallery.startFalleryInActivity(requestCode, this, FalleryOptions())
fun Fragment.startFallery(requestCode: Int) = Fallery.startFalleryInFragment(requestCode, this, FalleryOptions())