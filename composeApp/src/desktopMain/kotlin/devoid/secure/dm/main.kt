package devoid.secure.dm

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.compose.AsyncImage
import devoid.secure.dm.di.initKoin
import devoid.secure.dm.domain.model.User
import devoid.secure.dm.ui.compose.App
import devoid.secure.dm.ui.compose.AuthScreen
import devoid.secure.dm.ui.theme.SecureDmTheme
import devoid.secure.dm.ui.viewmodel.MainViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.java.KoinJavaComponent.inject
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.chat_bubble
import secure_dm.composeapp.generated.resources.document
import secure_dm.composeapp.generated.resources.person_edit

fun main() {
    initKoin()
    application {
        val viewModel:MainViewModel by inject(clazz = MainViewModel::class.java)
        val loading by viewModel.shouldShowSplashScreen.collectAsState()
        if(!loading){
            Window(
                onCloseRequest = ::exitApplication,
                icon = painterResource(Res.drawable.chat_bubble),
                title = "Secure-DM",
            ) {
                SecureDmTheme {
                    App(viewModel)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAuthScreen() {
    AuthScreen(
        isLogin = true,
        onNavigate = {
        },
        onAuthSuccess = { }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Preview
@Composable
fun PreviewProfile() {
    val user: User? = User(
        id = "test",
        email = "test@me.com",
        fullName = "John Doe",
        uName = "john_doe",
        bio = "I am a photographer! "
    )
    val profileUrl by derivedStateOf { user?.avatarUrl ?: Res.drawable.person_edit }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier = Modifier.size(62.dp).clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { },
                model = profileUrl,
                colorFilter =
                    if (user?.avatarUrl == null)
                        ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                    else null,
                contentDescription = null
            )
            Spacer(Modifier.width(16.dp))
            Text(text = user?.fullName ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.fillMaxWidth().weight(1f))
            OutlinedButton(onClick = {}){
                Text("Edit Profile")
            }
        }
        Text(text = user?.bio ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Card(Modifier.widthIn(max = 450.dp).fillMaxWidth(), elevation = 8.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row (Modifier.height(IntrinsicSize.Min)){
                    Text("Username",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.fillMaxWidth().weight(1f))
                    Text(text = user?.uName ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Text("Email",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.fillMaxWidth().weight(1f))
                    Text(text = user?.email ?: "", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
