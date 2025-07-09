package devoid.secure.dm.domain.files

import androidx.compose.runtime.Composable

@Composable
expect fun rememberDocumentPicker(onResult:(List<SharedFile>)->Unit): DocumentPicker



fun interface DocumentPicker{
    fun launch()
}