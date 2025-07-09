package devoid.secure.dm.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import devoid.secure.dm.ui.theme.backgroundMiddleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(avatarUri: Any, name: String, onNavigateBack: () -> Unit, onNavigateToProfile:()-> Unit) {
    var actionsMenuExpanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors()
                .copy(containerColor = MaterialTheme.colorScheme.backgroundMiddleColor.copy(alpha = 0.9f)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onNavigateToProfile)) {
                    AsyncImage(
                        model = avatarUri,
                        modifier = Modifier.size(30.dp).clip(CircleShape)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "navigate back")
                }
            }, actions = {
                IconButton(onClick = {actionsMenuExpanded = true}){
                    Icon(Icons.Rounded.MoreVert,contentDescription = null)
                }
                DropdownMenu(expanded = actionsMenuExpanded, onDismissRequest = {actionsMenuExpanded = false}, shape = MaterialTheme.shapes.medium){
                    DropdownMenuItem(text = {Text("Profile")}, onClick = onNavigateToProfile)
                    DropdownMenuItem(text = {Text("Files")}, onClick = onNavigateToProfile)
                    DropdownMenuItem(text = {Text("Close")}, onClick = onNavigateBack)
                }
            })
    }
}