package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devoid.secure.dm.data.datastore.SettingsSource
import devoid.secure.dm.domain.settings.SettingsConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsSource: SettingsSource) : ViewModel() {
    fun loadSettings(): Flow<SettingsConfig>{
        return settingsSource.getSettingsConfig()
    }

    fun saveSettings(settingsConfig: SettingsConfig) {
        viewModelScope.launch {
            settingsSource.saveSettings(settingsConfig)
        }
    }
}