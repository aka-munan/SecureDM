package devoid.secure.dm.ui.compose

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.date.TimeFormater
import devoid.secure.dm.ui.state.AudioPlayerState
import org.jetbrains.compose.resources.painterResource
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.play_arrow
import secure_dm.composeapp.generated.resources.stop
import kotlin.math.max
import kotlin.math.min


@Composable
fun RowScope.AudioPlayer(
    state:AudioPlayerState?,
    duration: Int,
    onSeek: (Float) -> Unit,
    onToggleState: () -> Unit
) {
    var seekAmount by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var progIndicatorMaxWidth = 1
    Card(onClick = onToggleState, shape = CircleShape) {
        when(state){
            is AudioPlayerState.Loading -> {
                CircularProgressIndicator(Modifier.size(24.dp).padding(4.dp), strokeWidth = 2.dp)
            }
            is AudioPlayerState.Ready -> {
                Icon(
                    modifier = Modifier.padding(4.dp),
                    painter = painterResource(if (state.isPlaying) Res.drawable.stop else Res.drawable.play_arrow),
                    contentDescription = null
                )

            }

            else -> {Icon(
                modifier = Modifier.padding(4.dp),
                painter = painterResource( Res.drawable.play_arrow),
                contentDescription = null
            )}
        }
    }
    LinearProgressIndicator(
        progress = { if (isDragging) seekAmount else if (state is AudioPlayerState.Ready) state.playerProgress else 0f},
        modifier = Modifier
            .height(10.dp)
            .weight(1f)
            .onSizeChanged { progIndicatorMaxWidth = it.width }
            .pointerInput(key1 = null) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        isDragging = false
                        onSeek(seekAmount)
                    },
                    onDragCancel = {
                        isDragging = false
                    }, onDragStart = {
                        if (state !is AudioPlayerState.Ready)
                            return@detectHorizontalDragGestures
                        isDragging = true
                    }) { _, dragAmount ->
                    seekAmount = min(max(0f, seekAmount + dragAmount / progIndicatorMaxWidth), 1f)
                    Logger.i("seek : $seekAmount")
                }
            },
    )
    val time = when(state){
        is AudioPlayerState.Ready -> {
            if(state.isPlaying) (duration * if (isDragging) seekAmount else state.playerProgress).toLong() else duration.toLong()
        }
        else->duration.toLong()
    }
    Text(
        TimeFormater.formatToString(time),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
        style = MaterialTheme.typography.labelMedium
    )

}
