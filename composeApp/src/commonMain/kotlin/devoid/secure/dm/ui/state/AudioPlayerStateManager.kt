package devoid.secure.dm.ui.state

import devoid.secure.dm.domain.model.MessageAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class AudioPlayerStateManager(val state: Flow<AudioPlayerState?>) {
    abstract fun onUpdateState(state: AudioPlayerState.Ready,attachment: MessageAttachment?)
}