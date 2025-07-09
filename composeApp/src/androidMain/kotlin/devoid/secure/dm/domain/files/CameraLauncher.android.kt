package devoid.secure.dm.domain.files

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger

@Composable
actual fun rememberCameraLauncher(onResult: (SharedFile) -> Unit): CameraLauncher {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    var capturedUri: Uri? by rememberSaveable{ mutableStateOf(null) }
    DisposableEffect(Unit) {
        onDispose { Logger.i("disposed") }
    }
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { captured ->
            if (captured && capturedUri != null) {
                onResult(SharedFileImpl(contentResolver, capturedUri.toString()))
                capturedUri = null
            }
        }
    return remember {
        CameraLauncher {
            capturedUri = FileProvider.getUriForFile(context,context.packageName+".provider",FileUtils.createTempFile(context, ".jpeg"))
            Logger.i("launching camera: $capturedUri")
            cameraLauncher.launch(capturedUri!!)
        }
    }
}

actual class CameraLauncher actual constructor(private val onLaunch: () -> Unit) {
    actual fun launch() {
        onLaunch()
    }
}