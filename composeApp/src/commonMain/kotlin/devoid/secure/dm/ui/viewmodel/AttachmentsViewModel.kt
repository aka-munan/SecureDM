package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.audio.AudioPLayerCallback
import devoid.secure.dm.domain.audio.AudioPlayer
import devoid.secure.dm.domain.files.AttachmentDownloader
import devoid.secure.dm.domain.files.DownloadStatus
import devoid.secure.dm.domain.model.AttachmentType
import devoid.secure.dm.domain.model.AttachmentsRepository
import devoid.secure.dm.domain.model.MessageAttachment
import devoid.secure.dm.ui.compose.PagingItems
import devoid.secure.dm.ui.compose.getPagedIntoState
import devoid.secure.dm.ui.state.AudioPlayerState
import devoid.secure.dm.ui.state.AudioPlayerStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AttachmentsViewModel(private val attachmentsRepository: AttachmentsRepository) : ViewModel() {
    val pageSize = 20
    private val TAG = "AttachmentViewModel"
    private val _attachments = MutableStateFlow<PagingItems<MessageAttachment>>(PagingItems())
    val attachments = _attachments.asStateFlow()
    private val audioPlayer = AudioPlayer()
    private val audioPlayerState = MutableStateFlow<AudioPlayerState?>(null)
    val audioPlayerStateManager  = object : AudioPlayerStateManager(audioPlayerState){
        override fun onUpdateState(
            state: AudioPlayerState.Ready,
            attachment: MessageAttachment?
        ) {
            updateAudioPlayerState(state,attachment)
        }

    }
    fun updateAudioPlayerState(state: AudioPlayerState.Ready,attachment: MessageAttachment?){
        try {
            if (state.id != audioPlayerState.value?.id) {//play request for different audio
                attachment?.run { initAudioPlayer(this, state.isPlaying) }
            }
            audioPlayer.seekTo((state.duration * state.playerProgress).toInt())
            if (audioPlayerState.value is AudioPlayerState.Ready) {
                if (state.isPlaying) {
                    if (state.playerProgress == 1f)
                        audioPlayer.seekTo(0)
                    audioPlayer.play()
                } else {
                    audioPlayer.pause()
                }
            }
        } catch (e: Exception) {
            Logger.e(e) {
                "failed to update audio player state"
            }
        }
    }
    private fun createAudioPlayerCallback(messageId: String?): AudioPLayerCallback {
        return object : AudioPLayerCallback {
            override fun onLoading() {
                audioPlayerState.update {
                    AudioPlayerState.Loading(messageId)
                }
            }

            override fun onReady() {
                audioPlayerState.update {
                    AudioPlayerState.Ready(messageId, false, audioPlayer.duration, 0f)
                }
            }

            override fun onPlay() {
                audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = true)
                    }?.getOrNull()
                }
            }

            override fun onPause() {
                audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = false)
                    }?.getOrNull()
                }
            }

            override fun onStop() {
                audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = false, playerProgress = 0f)
                    }?.getOrNull()
                }
            }

            override fun onProgress(fraction: Float) {
                audioPlayerState.update {
                    (it as AudioPlayerState.Ready?)?.copy(playerProgress = fraction)
                }
            }

        }
    }
    private fun initAudioPlayer(attachment: MessageAttachment, autoPlay: Boolean = true) {
        if (attachment.type != AttachmentType.AUDIO) {
            Logger.e(IllegalArgumentException("the attachment is not a type of AttachmentType.AUDIO")) {
                "failed to play audio"
            }
            return
        }
        viewModelScope.launch {
            if (audioPlayerState.value?.id != attachment.messageId) {
                audioPlayer.apply {
                    setCallback(createAudioPlayerCallback(attachment.messageId))
                    this.autoPlay = autoPlay
                    init(attachment.fileUri)
                }
            }
            audioPlayer.seekTo(0)
        }
    }
    fun downloadAttachment(attachment: MessageAttachment, onComplete: (Exception?) -> Unit) {
        viewModelScope.launch {
            AttachmentDownloader.downloadFile(attachment).collect { status ->
                when (status) {
                    DownloadStatus.Completed -> {
                        Logger.i(tag = TAG, messageString = "Attachment download successfully")
                        onComplete(null)
                    }

                    is DownloadStatus.Failure -> {
                        Logger.e(tag = TAG, status.e) {
                            "Attachment download failed"
                        }
                        onComplete(status.e)
                    }

                    is DownloadStatus.Progress -> {
                        Logger.i(tag = TAG, messageString = "download progress: ${status.progress * 100}")
                    }

                    DownloadStatus.Started -> {
                        Logger.i(tag = TAG, messageString = "Attachment download started")
                    }
                }
            }
        }
    }
    fun getAttachments(chatId: String, pageNumber: Int, vararg attachmentType: AttachmentType) {
        viewModelScope.launch {
            if (pageNumber==1){
                _attachments.value = PagingItems()
            }
            _attachments.value.getPagedIntoState(_attachments, pageSize, isRefreshRequest = pageNumber == 1) {
                attachmentsRepository.getAttachmentsSharedInChat(
                    chatId,
                    attachmentType = attachmentType,
                    pageNo = pageNumber,
                    pageSize = pageSize
                )
            }
        }
    }
}