package devoid.secure.dm.domain.settings

import androidx.compose.runtime.compositionLocalOf
import devoid.secure.dm.ui.compose.MessageStyle
import devoid.secure.dm.ui.theme.ThemePreference
import kotlinx.serialization.Serializable

@Serializable
data class SettingsConfig(
    val messageStyle: MessageStyle = MessageStyle.SENDER_ON_RHS,
    val linkPreview: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
)
val LocalSettingConfig = compositionLocalOf { SettingsConfig() }