@file:JvmName("Fallery")

package ir.mehdiyari.fallery.main.fallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ir.mehdiyari.fallery.main.di.FalleryCoreComponentHolder
import ir.mehdiyari.fallery.main.ui.FalleryActivity
import ir.mehdiyari.fallery.utils.FALLERY_CAPTION_KEY
import ir.mehdiyari.fallery.utils.FALLERY_MEDIAS_LIST_KEY

@JvmName("startFalleryFromActivityWithOptions")
@Deprecated(
    "Deprecated, Please use [registerFalleryResultCallback] instead.",
    level = DeprecationLevel.WARNING,
)
fun Activity.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) {
    FalleryCoreComponentHolder.createComponent(falleryOptions)
    startActivityForResult(Intent(this, FalleryActivity::class.java), requestCode)
}

@JvmName("registerFalleryResultCallback")
fun AppCompatActivity.registerFalleryResultCallback(
    onResult: (Array<String>?, String?) -> Unit,
): ActivityResultLauncher<FalleryOptions> = registerForActivityResult(object :
    ActivityResultContract<FalleryOptions, Pair<Array<String>?, String?>>() {
    override fun createIntent(context: Context, input: FalleryOptions): Intent {
        FalleryCoreComponentHolder.createComponent(input)
        return Intent(context, FalleryActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Array<String>?, String?> {
        return intent?.getFalleryResultMediasFromIntent() to intent?.getFalleryCaptionFromIntent()
    }
}) {
    onResult(it.first, it.second)
}

@JvmName("startFalleryFromFragmentWithOptions")
@Deprecated(
    "Deprecated, Please use [registerFalleryResultCallback] instead.",
    level = DeprecationLevel.WARNING,
)
fun Fragment.startFalleryWithOptions(requestCode: Int, falleryOptions: FalleryOptions) {
    FalleryCoreComponentHolder.createComponent(falleryOptions)
    startActivityForResult(Intent(this.requireContext(), FalleryActivity::class.java), requestCode)
}

@JvmName("registerFalleryResultCallback")
fun Fragment.registerFalleryResultCallback(
    onResult: (Array<String>?, String?) -> Unit,
): ActivityResultLauncher<FalleryOptions> = registerForActivityResult(object :
    ActivityResultContract<FalleryOptions, Pair<Array<String>?, String?>>() {
    override fun createIntent(context: Context, input: FalleryOptions): Intent {
        FalleryCoreComponentHolder.createComponent(input)
        return Intent(context, FalleryActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Array<String>?, String?> {
        return intent?.getFalleryResultMediasFromIntent() to intent?.getFalleryCaptionFromIntent()
    }
}) {
    onResult(it.first, it.second)
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