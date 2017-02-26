package sergiomoura.com.photooverlay.common.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity

open class RuntimePermissionsManager(private val activity: AppCompatActivity) {

    companion object {
        const val PERMISSIONS_REQUEST_STORAGE_CAMERA = 2

        fun allPermissionsGranted(grantResults: IntArray): Boolean {
            return grantResults.size > 0 && grantResults.none { it == PackageManager.PERMISSION_DENIED }
        }
    }

    fun checkPermission(permissionsList: Array<String>) {
        val requestCode = permissionTranslation(permissionsList)
        val permissionsToAsk = permissionsList.filter { !hasPermission(it) }.toTypedArray()
        val grantResults = permissionsList.map {
            if (hasPermission(it)) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        }

        if (permissionsToAsk.any()) {
            activity.requestPermissions(permissionsToAsk, requestCode)
        } else {
            activity.onRequestPermissionsResult(requestCode, permissionsList, grantResults.toIntArray())
        }
    }

    fun permissionTranslation(permissionsList: Array<String>): Int {
        return when {
            permissionsList.toList().containsAll(listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) -> PERMISSIONS_REQUEST_STORAGE_CAMERA
            else -> -1
        }
    }

    fun requestCameraAndStoragePermissions() = checkPermission(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE))

    fun hasPermission(permission: String) = activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
