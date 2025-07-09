package devoid.secure.dm.data.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.settings.SettingsConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class SettingsSource(private val dataStore: PrefsDataStore) {
    private val TAG = "SettingSource"
    companion object {
        private const val SETTINGS_KEY = "settings_key"
    }

    fun getSettingsConfig(): Flow<SettingsConfig> {
        return dataStore.data.map {
            val json = it[stringPreferencesKey(SETTINGS_KEY)] ?: return@map SettingsConfig()
            Json.decodeFromString(json)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveSettings(settingsConfig: SettingsConfig) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit {
                    val json = Json.encodeToString(settingsConfig)
                    it[stringPreferencesKey(SETTINGS_KEY)] = json
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.e(tag = TAG, throwable = e, messageString = "error occurred")
            }
        }
    }
}
