package devoid.secure.dm.data.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File


@Composable
actual fun rememberDatastore(): PrefsDataStore {
    return remember {
        createDataStore(
            producePath = {
                val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
                file.absolutePath
            })
    }
}
fun createDatastore(): PrefsDataStore{
    return createDataStore(
        producePath = {
            val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
            file.absolutePath
        })
}