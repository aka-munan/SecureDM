package devoid.secure.dm.domain

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionsManager(callback: PermissionCallback): PermissionsManager {
    return PermissionsManager(callback)
}

// PermissionsManager.kt
actual class PermissionsManager actual constructor(callback: PermissionCallback) :
    PermissionHandler {
    @Composable
    override fun askPermission(permission: PermissionType) {
        when(permission){
            PermissionType.GALLERY -> TODO()
            PermissionType.CAMERA -> TODO()
            PermissionType.RECORD_AUDIO -> TODO()
        }
    }

    @Composable
    override fun isPermissionGranted(permission: PermissionType): Boolean {
       return when(permission){
            PermissionType.GALLERY -> TODO()
            PermissionType.CAMERA -> TODO()
            PermissionType.RECORD_AUDIO -> true
        }
    }

    override fun launchSettings() {
        TODO("Not yet implemented")
    }
}