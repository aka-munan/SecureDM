package devoid.secure.dm.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.content_copy
import secure_dm.composeapp.generated.resources.download
import secure_dm.composeapp.generated.resources.reply

@Composable
fun MessageDropDown(
    modifier: Modifier = Modifier,
    state: MessageDropDownState,
    onDismiss: () -> Unit,
    date: String
) {
    DropdownMenu(
        modifier = modifier,
        expanded = state.isVisible,
        shape = MaterialTheme.shapes.medium,
        onDismissRequest = onDismiss,
        offset = state.offset
    ) {
        Text(
            date,//00-may-2025
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        )
        if (state.canReply) {
            DropdownMenuItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = { Text("Reply") },
                leadingIcon = {
                    Icon(
                        painterResource(Res.drawable.reply),
                        contentDescription = null,
                    )
                },
                onClick = { state.callback?.onReply() })
        }
        if (state.containsText) {
            DropdownMenuItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = { Text("Copy") },
                leadingIcon = {
                    Icon(
                        painterResource(Res.drawable.content_copy),
                        contentDescription = null,
                    )
                },
                onClick = { state.callback?.onCopy() })
        }
        if (state.canUnsend) {
            DropdownMenuItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = { Text("Unsend", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = { state.callback?.onUnsend() })
        }
        if (state.canDownload) {
            DropdownMenuItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = { Text("Download") },
                leadingIcon = {
                    Icon(
                        painterResource(Res.drawable.download),
                        contentDescription = null
                    )
                },
                onClick = { state.callback?.onDownload() })
        }
    }

}

interface MessageDropDownCallback {
    fun onReply()
    fun onCopy()
    fun onUnsend()
    fun onDownload()
}

class MessageDropDownState() {
    var isVisible by mutableStateOf(false)
    var callback: MessageDropDownCallback? = null
    var canUnsend = false
    var containsText = false
    var canDownload = false
    var canReply = true
    var offset: DpOffset = DpOffset.Unspecified
}

@Composable
fun rememberMessageDropDownState(callback: MessageDropDownCallback? = null): MessageDropDownState {
    return remember { MessageDropDownState().apply { this.callback = callback } }
}

fun Modifier.rightClickMenu(onRightClick: PointerInputScope.(offset: Offset) -> Unit) =
    this.then(Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                    onRightClick(event.changes.last().position)
                }
            }
        }
    })