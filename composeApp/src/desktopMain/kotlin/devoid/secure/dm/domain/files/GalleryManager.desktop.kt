package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter


@Composable
actual fun rememberGalleryManager(onResult: (SharedFile?) -> Unit): GalleryManager {
    val fileChooser = JFileChooser().apply {
        isMultiSelectionEnabled = false
        dialogTitle = "Select image to upload"
        approveButtonText = "Select"
        isAcceptAllFileFilterUsed = false
        fileFilter = object: FileFilter() {
            override fun accept(f: File): Boolean {
                return f.isDirectory || f.extension.lowercase() in ImageExtensions
            }

            override fun getDescription(): String? = "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp, *.webp)"

        }
    }
    return GalleryManager {
        val result  = fileChooser.showOpenDialog(null)
        if(result == JFileChooser.APPROVE_OPTION){
            fileChooser.selectedFile.apply {
                Logger.i("file picked: $absolutePath")
                onResult(SharedFileImpl(this.toURI().toString()))
            }
        }
    }
}
