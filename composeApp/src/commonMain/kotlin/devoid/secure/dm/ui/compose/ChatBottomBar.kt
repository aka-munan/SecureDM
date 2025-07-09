package devoid.secure.dm.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import devoid.secure.dm.ui.state.AudioPlayerState
import devoid.secure.dm.ui.theme.secondaryBackgroundBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import secure_dm.composeapp.generated.resources.*
import kotlin.math.max

@Composable
fun AudioRecording(recordedDuration: String, onStop: () -> Unit) {
    val scope = rememberCoroutineScope()
    val recordingText by produceState("recording...") {
        val delay = 400L
        while (true) {
            value = "recording"
            delay(delay)
            value = "recording."
            delay(delay)
            value = "recording.."
            delay(delay)
            value = "recording..."
            delay(delay)
        }
    }
    scope.launch {
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.tertiary)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(30.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            .padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.circle_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary
        )
        Text(
            recordingText,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f)
        )
        Text(
            recordedDuration,
            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelMedium
        )
        Card(onClick = onStop, shape = CircleShape) {
            Icon(
                modifier = Modifier.padding(6.dp),
                painter = painterResource(Res.drawable.stop),
                contentDescription = null
            )
        }
    }
}

@Composable
fun AudioRecorded(
    audioPlayerState: AudioPlayerState?,
    duration: Int,
    onSeek: (Float) -> Unit,
    onTogglePlayerState: () -> Unit,
    onDelete: () -> Unit,
    onSend: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.filledTonalIconButtonColors()
        ) {
            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.secondaryBackgroundBrush())
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(30.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                ).padding(vertical = 4.dp).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioPlayer(
                state = audioPlayerState,
                duration = duration,
                onSeek = onSeek,
                onToggleState = onTogglePlayerState
            )
            Button(
                enabled = true, onClick = onSend
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "send message"
                )
            }
        }
    }
}

@Composable
fun MessageComposer(
    modifier: Modifier = Modifier,
    text: String,
    onTextMessageChange: (String) -> Unit,
    previews: @Composable ColumnScope.() -> Unit,
    attachmentsPicked: Boolean,
    onSendBtnClick: () -> Unit,
    onLaunchGallery: () -> Unit,
    onRecordAudio: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        previews()
        Row(verticalAlignment = Alignment.CenterVertically) {

            TextField(
                modifier = Modifier.fillMaxWidth().weight(1f).onKeyEvent {
                    if (text.isNotBlank() && it.key == Key.Enter && it.type == KeyEventType.KeyDown && it.isCtrlPressed) {
                        onSendBtnClick.invoke()
                        true
                    } else true
                },
                value = text,
                onValueChange = onTextMessageChange,
                maxLines = 5,
                placeholder = { Text("Message..") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,

                    )
            )
            AnimatedContent(targetState = text.isEmpty() && !attachmentsPicked) {//send btn and audio+image quick action
                if (it) {
                    Row {
                        IconButton(onClick = onRecordAudio) {
                            Image(
                                modifier = Modifier.padding(4.dp),
                                painter = painterResource(Res.drawable.mic),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(color = LocalContentColor.current)
                            )
                        }
                        IconButton(onClick = onLaunchGallery) {
                            Image(
                                modifier = Modifier.padding(4.dp),
                                painter = painterResource(Res.drawable.image),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(color = LocalContentColor.current)
                            )
                        }
                    }
                } else {
                    Button(
                        enabled = true, onClick = onSendBtnClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "send message"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UrlPreview(modifier: Modifier = Modifier, title: String?, subTitle: String?, image: String?, vertical: Boolean) {
        Row (modifier){
            if (!vertical) AsyncImage(model = image, modifier= Modifier.widthIn(max=200.dp).wrapContentHeight(),contentDescription = null)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                if (vertical) AsyncImage(model = image, contentDescription = null)
                Text(
                    title ?: "",
                    maxLines = 2,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subTitle ?: "",
                    maxLines = 4,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            }
        }
}