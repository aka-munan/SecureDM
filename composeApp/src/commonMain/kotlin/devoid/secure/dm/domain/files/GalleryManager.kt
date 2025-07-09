package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable

@Composable
expect fun rememberGalleryManager(onResult:(SharedFile?)->Unit): GalleryManager

fun interface GalleryManager{
    fun launch()
}
val ImageExtensions =  listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")