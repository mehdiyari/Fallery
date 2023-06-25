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
    onResult: (FalleryResult) -> Unit,
): ActivityResultLauncher<FalleryOptions> = registerForActivityResult(
    getFalleryActivityResultContract(), onResult
)

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
    onResult: (FalleryResult) -> Unit,
): ActivityResultLauncher<FalleryOptions> = registerForActivityResult(
    getFalleryActivityResultContract(), onResult
)

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


/**
 * API for getting [ActivityResultContract] for register activityResult.
 * This API can be used inside composable functions for starting fallery and get the result.
 *
 * <p>
 * <pre>
 *
 *      val falleryLauncher = rememberLauncherForActivityResult(
 *             getFalleryActivityResultContract()
 *      ) { result ->
 *             doSomethingWithPhotosAndCaption(result.mediaPathList, result.caption)
 *      }
 *
 *      Button(onClick = { falleryLauncher.launch(falleryOptions) }) {
 *             Text(text = "Open Fallery")
 *      }
 *
 * </pre>
 * </p>
 *
 */
@JvmName("getFalleryActivityResultContract")
fun getFalleryActivityResultContract(): ActivityResultContract<FalleryOptions, FalleryResult> {
    return object : ActivityResultContract<FalleryOptions, FalleryResult>() {
        override fun createIntent(context: Context, input: FalleryOptions): Intent {
            FalleryCoreComponentHolder.createComponent(input)
            return Intent(context, FalleryActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): FalleryResult {
            return FalleryResult(
                intent?.getFalleryResultMediasFromIntent()?.toList(),
                intent?.getFalleryCaptionFromIntent()
            )
        }
    }
}