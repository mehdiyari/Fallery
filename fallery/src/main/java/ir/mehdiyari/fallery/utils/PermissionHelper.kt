package ir.mehdiyari.fallery.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity


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

internal inline fun AppCompatActivity.requestSharedStoragePermission(granted: () -> Unit, denied: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            granted.invoke()
        } else {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).also {
                it.data = Uri.fromParts("package", packageName, null)
                startActivity(it)
            }
            denied.invoke()
        }
    } else {
        granted.invoke()
    }
}