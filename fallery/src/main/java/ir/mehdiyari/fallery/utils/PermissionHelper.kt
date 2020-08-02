package ir.mehdiyari.fallery.utils

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity


internal inline fun AppCompatActivity.permissionChecker(
    permission: String,
    granted: () -> Unit = {},
    denied: () -> Unit = {}
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            denied()
        } else {
            granted()
        }
    } else {
        granted()
    }
}

internal inline fun FragmentActivity.permissionChecker(
    permission: String, granted: () -> Unit, denied: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            denied()
        } else {
            granted()
        }
    } else {
        granted()
    }
}