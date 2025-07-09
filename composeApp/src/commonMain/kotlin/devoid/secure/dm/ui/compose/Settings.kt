package devoid.secure.dm.ui.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import devoid.secure.dm.domain.settings.SettingsConfig
import devoid.secure.dm.ui.theme.ThemePreference
import devoid.secure.dm.ui.theme.blurColor
import devoid.secure.dm.ui.viewmodel.AuthViewModel
import devoid.secure.dm.ui.viewmodel.LoginProvider
import devoid.secure.dm.ui.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import secure_dm.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<AuthViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val originalConfig by settingsViewModel.loadSettings().collectAsState(SettingsConfig())
    var editedConfig by remember(originalConfig) { mutableStateOf(originalConfig) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(title = {
                Text("Settings")
            },colors = TopAppBarDefaults.topAppBarColors().copy(
                containerColor = MaterialTheme.colorScheme.blurColor
            ), actions = {
                Button(
                    enabled = originalConfig != editedConfig, content = {
                        Text("Save")
                    },
                    onClick = {
                        settingsViewModel.saveSettings(editedConfig)
                    }
                )
            })
        }) {paddingValues ->
        Box(modifier = modifier.then(Modifier.padding(paddingValues)), contentAlignment = Alignment.Center) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.blurColor)) {
                    Column(Modifier.padding(4.dp)) {
                        Text(
                            "Chats",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SettingComponent(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            title = "Link Preview",
                            subTitle = "Enable previewing urls in the chat."
                        ) {
                            Switch(
                                checked = editedConfig.linkPreview,
                                onCheckedChange = {
                                    editedConfig = editedConfig.copy(linkPreview = !editedConfig.linkPreview)
                                })
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        SettingComponent(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            title = "Chats layout",
                            subTitle = "Set preference for the alignment of messages sent."
                        ) {
                            val isLtr = editedConfig.messageStyle == MessageStyle.SENDER_ON_LHS
                            IconToggleButton(
                                checked = isLtr,
                                onCheckedChange = {
                                    editedConfig =
                                        editedConfig.copy(
                                            messageStyle = if (isLtr)
                                                MessageStyle.SENDER_ON_RHS else MessageStyle.SENDER_ON_LHS
                                        )
                                }) {
                                if (isLtr) {
                                    Icon(
                                        modifier = Modifier.clip(CircleShape),
                                        painter = painterResource(Res.drawable.align_left),
                                        contentDescription = null
                                    )
                                } else {
                                    Icon(
                                        modifier = Modifier.clip(CircleShape),
                                        contentDescription = null,
                                        painter = painterResource(Res.drawable.align_right)
                                    )
                                }
                            }
                        }
                    }
                }
                Card (colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.blurColor)){
                    Column(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                        Text(
                            "Theme",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "Set Theme preference for this app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                            var dropDownExpanded by remember { mutableStateOf(false) }
                            Spinner(
                                ThemePreference.entries,
                                editedConfig.themePreference.name.lowercase(),
                                dropDownExpanded,
                                onChangeState = {
                                    dropDownExpanded = it
                                },
                                onItemSelected = {
                                    editedConfig = editedConfig.copy(themePreference = it)
                                })
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.blurColor)) {
                    Column(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        val user by viewModel.currentUser.collectAsState()
                        ProfileDisplayer(
                            viewModel.getLoginProvider(),
                            user?.fullName ?: "Unknown User",
                            user?.avatarUrl,
                            onLogout = {
                                viewModel.signOut { }
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDisplayer(loginProvider: LoginProvider, name: String, avtar: String?, onLogout: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            modifier = Modifier.clip(CircleShape).size(50.dp),
            model = avtar,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.padding(8.dp).fillMaxWidth().weight(1f)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {

                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    VerticalDivider(Modifier.height(28.dp).padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    val icon = if (loginProvider == LoginProvider.GOOGLE)
                        Res.drawable.google_logo else Res.drawable.passkey
                    val text = "logged in using ${loginProvider.name}"
                    Image(
                        modifier = Modifier.clip(CircleShape).size(28.dp),
                        painter = painterResource(icon),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text, maxLines = 1, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
                }
            }
        }
        Button(onClick = onLogout) {
            Text("Logout")
        }
    }
}

@Composable
fun <T> Spinner(
    items: Collection<T>,
    defaultText: String,
    dropDownExpanded: Boolean,
    onChangeState: (visible: Boolean) -> Unit,
    onItemSelected: (T) -> Unit
) {
    val animatedRotation by animateFloatAsState(if (dropDownExpanded) -180f else 0f)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = { onChangeState(true) }) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(defaultText, style = MaterialTheme.typography.bodyMedium)
            Icon(
                modifier = Modifier.rotate(animatedRotation),
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null
            )
        }
        DropdownMenu(expanded = dropDownExpanded, onDismissRequest = { onChangeState(false) }) {
            items.forEach {
                DropdownMenuItem(text = {
                    Text("$it".lowercase())
                }, onClick = {
                    onChangeState(false)
                    onItemSelected(it)
                })
            }
        }
    }
}

@Composable
fun SettingComponent(modifier: Modifier = Modifier, title: String, subTitle: String, action: @Composable () -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min).then(modifier), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.weight(1f).widthIn(min = 4.dp))
        action()
    }

}

