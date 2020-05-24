package ir.mehdiyari.fallery.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.animation.Animation
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

fun divideScreenToEqualPart(
    @IntRange(from = 1) displayWidth: Int,
    @FloatRange(from = 1.toDouble()) itemWidth: Float,
    @IntRange(from = 1) minCount: Int
): Int = Math.floor((displayWidth / itemWidth).toDouble()).toInt().let {
    return if (it == 0)
        minCount
    else
        it
}

fun dpToPx(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()
fun pxToDp(px: Int): Int = (px / Resources.getSystem().displayMetrics.density).toInt()
fun getSettingIntent(activity: Context): Intent = Intent().apply {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", activity.packageName, null)
}

fun isAndroidTenOrHigher(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

internal fun Animation.setOnAnimationEndListener(onEnd: () -> Unit) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) = Unit
        override fun onAnimationEnd(animation: Animation?) = onEnd()
        override fun onAnimationStart(animation: Animation?) = Unit
    })
}