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
        TODO("Not yet implemented")
    }

    @Composable
    override fun isPermissionGranted(permission: PermissionType): Boolean {
        TODO("Not yet implemented")
    }

    @Composable
    override fun launchSettings() {
        TODO("Not yet implemented")
    }
}