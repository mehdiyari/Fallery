@file:JvmName("Fallery")

package ir.mehdiyari.fallery.main.fallery

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryActivity
import ir.mehdiyari.fallery.utils.FALLERY_CAPTION_KEY
import ir.mehdiyari.fallery.utils.FALLERY_MEDIAS_LIST_KEY

@JvmName("startFalleryFromActivityWithOptions")
fun Activity.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) {
    FalleryCoreComponentHolder.createComponent(falleryOptions)
    startActivityForResult(Intent(this, FalleryActivity::class.java), requestCode)
}

@JvmName("startFalleryFromFragmentWithOptions")
fun Fragment.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) {
    FalleryCoreComponentHolder.createComponent(falleryOptions)
    startActivityForResult(Intent(this.requireContext(), FalleryActivity::class.java), requestCode)
}

@JvmName("getResultMediasFromIntent")
fun Intent.getFalleryResultMediasFromIntent(): Array<String>? {
    if (this.hasExtra(FALLERY_MEDIAS_LIST_KEY)) {
        return this.getStringArrayExtra(FALLERY_MEDIAS_LIST_KEY)
    } else {
        throw IllegalArgumentException("input intent has no extra with key $FALLERY_MEDIAS_LIST_KEY")
    }
}

@JvmName("getCaptionFromIntent")
fun Intent.getFalleryCaptionFromIntent(): String? = this.getStringExtra(FALLERY_CAPTION_KEY)