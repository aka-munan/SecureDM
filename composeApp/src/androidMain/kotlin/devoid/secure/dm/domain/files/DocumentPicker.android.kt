package devoid.secure.dm.domain.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat

@Composable
actual fun rememberDocumentPicker(onResult: (List<SharedFile>) -> Unit): DocumentPicker {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val docPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenMultipleDocuments()) {uris->
        onResult(uris.map { SharedFileImpl(contentResolver,it.toString()) })
    }
    return remember{
        DocumentPicker {
            docPicker.launch(arrayOf("application/*","image/*","video/*"), options = ActivityOptionsCompat.makeBasic())
        }
    }
}

