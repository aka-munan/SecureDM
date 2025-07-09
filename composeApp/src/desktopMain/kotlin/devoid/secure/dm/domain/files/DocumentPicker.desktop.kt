package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import javax.swing.JFileChooser


@Composable
actual fun rememberDocumentPicker(onResult: (List<SharedFile>) -> Unit): DocumentPicker {
//    var isVisible by mutableStateOf(true)
//    Window(onCloseRequest = { isVisible = false; }, title = "pick a file") {
//        if (isVisible) {
//            FileDialog(this.window, "", FileDialog.LOAD)
//        }
//    }
    val fileChooser = JFileChooser().apply {
        isMultiSelectionEnabled = true
        dialogTitle = "Select files to upload"
        approveButtonText = "Select"

    }
    return DocumentPicker {
        val result  = fileChooser.showOpenDialog(null)
        if(result == JFileChooser.APPROVE_OPTION){
           fileChooser.selectedFiles.apply {
               Logger.i("file picked"+ map{it.absolutePath})
               onResult(map { file -> SharedFileImpl(file.toURI().toString()) })
           }

        }
    }
}

