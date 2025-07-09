package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalContext

@Composable
expect fun rememberCameraLauncher(onResult:(SharedFile)->Unit):CameraLauncher

expect class CameraLauncher(onLaunch:()->Unit){
    fun launch()
}