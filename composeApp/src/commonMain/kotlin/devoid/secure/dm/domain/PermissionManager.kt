package devoid.secure.dm.domain

import androidx.compose.runtime.Composable

expect class PermissionsManager(callback: PermissionCallback) : PermissionHandler

interface PermissionCallback {
    fun onPermissionStatus(permissionType: PermissionType, status: PermissionStatus)
}

@Composable
expect fun rememberPermissionsManager(callback: PermissionCallback): PermissionsManager

interface PermissionHandler {
    @Composable
    fun askPermission(permission: PermissionType)

    @Composable
    fun isPermissionGranted(permission: PermissionType): Boolean

    fun launchSettings()

}

enum class PermissionStatus{
    GRANTED, DENIED, SHOW_RATIONAL
}
enum class PermissionType{
    GALLERY, CAMERA ,RECORD_AUDIO
}