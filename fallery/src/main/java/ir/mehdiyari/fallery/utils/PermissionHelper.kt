package ir.mehdiyari.fallery.utils

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

internal inline fun AppCompatActivity.permissionChecker(
    permissions: Array<String>,
    onAllGranted: () -> Unit = {},
    onDenied: (List<String>) -> Unit = {}
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val notGranted = mutableListOf<String>()
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permission)
            }
        }

        if (notGranted.isNotEmpty()) {
            onDenied(notGranted)
        } else {
            onAllGranted()
        }
    } else {
        onAllGranted()
    }
}