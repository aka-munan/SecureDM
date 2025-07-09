package devoid.secure.dm.domain.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


@Composable
actual fun rememberGalleryManager(onResult: (SharedFile?) -> Unit): GalleryManager {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            onResult.invoke(SharedFileImpl(contentResolver, it.toString()))
        }
    }
    return remember {
        GalleryManager {
            galleryLauncher.launch(
                PickVisualMediaRequest(
                    mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            )
        }
    }
}