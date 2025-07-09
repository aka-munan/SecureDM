package devoid.secure.dm.domain

import androidx.compose.ui.graphics.ImageBitmap
import devoid.secure.dm.domain.files.GalleryManager

actual class SharedImage {
    actual fun toByteArray(): ByteArray? {
        TODO("Not yet implemented")
    }

    actual fun toImageBitmap(): ImageBitmap? {
        TODO("Not yet implemented")
    }
}

actual class GalleryManager actual constructor(onLaunch: () -> Unit) {
    actual fun launch() {
    }

    actual fun onResult(result: SharedImage?) {
    }
}

actual fun rememberGalleryManager(): GalleryManager {
    TODO("Not yet implemented")
}