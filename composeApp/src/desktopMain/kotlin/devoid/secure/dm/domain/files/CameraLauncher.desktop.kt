package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable


@Composable
actual fun rememberCameraLauncher(onResult: (SharedFile) -> Unit): CameraLauncher {
    return CameraLauncher {  }
}

actual class CameraLauncher actual constructor(onLaunch: () -> Unit) {
    actual fun launch() {
    }
}