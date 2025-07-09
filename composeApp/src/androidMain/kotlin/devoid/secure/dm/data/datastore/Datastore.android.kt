package devoid.secure.dm.data.datastore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDatastore(): PrefsDataStore {
    val context = LocalContext.current
    return remember {
        createDataStore(
            producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath })
    }
}
fun createDataStore(context: Context): PrefsDataStore{
     return createDataStore(
        producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath })
}