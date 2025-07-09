package devoid.secure.dm.domain

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

@Composable
actual fun rememberPermissionsManager(callback: PermissionCallback): PermissionsManager {
    return remember { PermissionsManager(callback)}
}

// PermissionsManager.kt
actual class PermissionsManager actual constructor(private val callback: PermissionCallback) :
    PermissionHandler {
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun askPermission(permission: PermissionType) {
        val lifecycleOwner = LocalLifecycleOwner.current
        when (permission) {
            PermissionType.CAMERA -> {
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                LaunchedEffect(cameraPermissionState) {
                    val permissionResult = cameraPermissionState.status
                    if (!permissionResult.isGranted) {
                        if (permissionResult.shouldShowRationale) {
                            callback.onPermissionStatus(
                                permission, PermissionStatus.SHOW_RATIONAL
                            )
                        } else {
                            lifecycleOwner.lifecycleScope.launch {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    } else {
                        callback.onPermissionStatus(
                            permission, PermissionStatus.GRANTED
                        )
                    }
                }
            }

            PermissionType.GALLERY -> {
                // Granted by default because in Android GetContent API does not require any
                callback.onPermissionStatus(
                    permission, PermissionStatus.GRANTED
                )
            }

            PermissionType.RECORD_AUDIO -> {
                val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
                LaunchedEffect(recordAudioPermissionState) {
                    val permissionResult = recordAudioPermissionState.status
                    if (permissionResult.isGranted) {
                        callback.onPermissionStatus(
                            permission, PermissionStatus.GRANTED
                        )
                    } else {
                      if (permissionResult.shouldShowRationale) {
                            callback.onPermissionStatus(
                                permission, PermissionStatus.SHOW_RATIONAL
                            )
                        }else {
                            lifecycleOwner.lifecycleScope.launch {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun isPermissionGranted(permission: PermissionType): Boolean {
        return when(permission){
            // Granted by default because in Android GetContent API does not require any
            PermissionType.GALLERY->true

            PermissionType.CAMERA ->{
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                cameraPermissionState.status.isGranted
            }

            PermissionType.RECORD_AUDIO -> {
                val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
                recordAudioPermissionState.status.isGranted
            }
        }
    }

    override fun launchSettings() {
        val context = GlobalContext.get().get<Context>()
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}